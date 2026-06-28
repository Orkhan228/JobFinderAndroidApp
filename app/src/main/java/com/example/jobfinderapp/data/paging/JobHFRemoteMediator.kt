package com.example.jobfinderapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.jobfinderapp.data.db.JobDatabase
import com.example.jobfinderapp.data.entity.JobRemoteKeysEntity
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.network.RetrofitService
import com.example.jobfinderapp.entity.JobFilter
import com.example.jobfinderapp.utils.AppLogger
import com.example.jobfinderapp.utils.SettingsDataStore
import com.example.jobfinderapp.utils.releaseNullable
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class JobHFRemoteMediator (
    private val api: RetrofitService,
    private val database: JobDatabase,
    private val jobFilter: JobFilter,
) : RemoteMediator<Int, JobUIModel>() {

    private val remoteKeysDao = database.remoteKeysDao()
    private val jobDao = database.jobDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, JobUIModel>,
    ): MediatorResult {
        try {
            val page = when (loadType) {
                LoadType.APPEND -> {

                    // 1. Ищем последнюю страницу safely
                    val lastPage = state.pages.lastOrNull { it.data.isNotEmpty() }

                    // Если страниц пока нет в памяти PagingState, это НЕ значит, что список кончился!
                    // Это значит, что Room еще не успел отдать данные. Говорим false, чтобы Paging повторил попытку позже.
                    if (lastPage == null) {
                        return MediatorResult.Success(endOfPaginationReached = false)
                    }

                    val lastItem = lastPage.data.lastOrNull()
                        ?: return MediatorResult.Success(endOfPaginationReached = false)

                    // 2. Достаем ключи
                    val remoteKeys = remoteKeysDao.getRemoteKeysByJobID(lastItem.job.id)

                    // Если ключей нет в базе — это аномалия (база еще не записала или сбой).
                    // Возвращаем Error(истинная причина), либо Success(false), но ЛУЧШЕ Error, чтобы сработал retry()!
                    if (remoteKeys == null) {
                        AppLogger.e("JobHFRemoteMediator", "RemoteKey missing for ID: ${lastItem.job.id}")
                        return MediatorResult.Success(false)
                    }

                    val nextKey = remoteKeys.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = true)

                    nextKey
                }

                LoadType.REFRESH -> {
                    AppLogger.d("REFRESH_DIAGNOSTIC", "1. Сработал REFRESH в медиаторе!")
                    1
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            val response = api.getFilteredList(
                countryCode = jobFilter.country.code,
                page = page,
                searchKeyWords = jobFilter.searchKeyWords,
                categoryTag = jobFilter.category?.tag,
                sortDirection = jobFilter.sortDirection,
                sortBy = jobFilter.sortBy,
                onlyFullTime = if(jobFilter.onlyFullTime) "1" else null,
                onlyPartTime = if (jobFilter.onlyPartTime) "1" else null,
                onlyContractJobs = if (jobFilter.onlyContractJobs) "1" else null,
                onlyPermanentJobs = if (jobFilter.onlyPermanentJobs) "1" else null,
                location0 = jobFilter.locations?.getOrNull(0),
                location1 = jobFilter.locations?.getOrNull(1),
                location2 = jobFilter.locations?.getOrNull(2),
                location3 = jobFilter.locations?.getOrNull(3),
                location4 = jobFilter.locations?.getOrNull(4),
                location5 = jobFilter.locations?.getOrNull(5),
            )

            if (!response.isSuccessful) {
                AppLogger.e("JobHFRemoteMediator", "The response from server is unsuccessful",
                    HttpException(response))

                return MediatorResult.Error(HttpException(response))
            }

            val body = response.body()
                ?: return MediatorResult.Error(IllegalStateException("The body of response is null"))

            val jobResults = body.results
                ?: return MediatorResult.Error(IllegalStateException("Results are null"))

            val endOfPaginationReached = jobResults.isEmpty()

            val jobReleased = jobResults.map {
                it.releaseNullable()
            }

            AppLogger.d("REFRESH_DIAGNOSTIC", "Параметры запроса -> Поиск: '${jobFilter.searchKeyWords}', Страна: '${jobFilter.country.code}'")
            AppLogger.d("REFRESH_DIAGNOSTIC", "ID вакансий с сервера: ${jobReleased.map { it.id }}")

            val nextKey = if (endOfPaginationReached) null else page + 1
            val prevKey = if (page == 1) null else page - 1

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    AppLogger.d("REFRESH_DIAGNOSTIC", "2. Очищаем базу данных...")
                    remoteKeysDao.clearAllRemoteKeys()
                    jobDao.clearDB()
                    AppLogger.d("REFRESH_DIAGNOSTIC", "3. База очищена. Новых вакансий с сервера пришло: ${jobReleased.size}")
                }

                remoteKeysDao.insertRemoteKeys(jobReleased.map {
                    JobRemoteKeysEntity(
                        jobId = it.id,
                        nextKey = nextKey,
                        prevKey = prevKey
                    )
                })

                jobDao.insertJobs(jobReleased)
            }

//            settingsDataStore.saveCurrentTimeToDataStore()

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            else {
                AppLogger.e("JobHFRemoteMediator", "Exception in method load", e)
            }
            return MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
//        val lastUpdateTime = settingsDataStore.getLastUpdateTimeFromDataStore()
//        val now = System.currentTimeMillis()
//
//        return if (now - lastUpdateTime >= CACHE_TIMEOUT) {
//            InitializeAction.LAUNCH_INITIAL_REFRESH
//        } else {
//            InitializeAction.SKIP_INITIAL_REFRESH
//        }
    }

//    private suspend fun getRemoteKeyClosestToCurrentPosition(
//        state: PagingState<Int, JobUIModel>
//    ): JobRemoteKeysEntity? {
//        return state.anchorPosition?.let { position ->
//            state.closestItemToPosition(position)?.let { jobUIModel ->
//                val jobId = jobUIModel.job.id
//                remoteKeysDao.getRemoteKeysByJobID(jobId)
//            }
//        }
//    }

//    companion object {
//        const val CACHE_TIMEOUT = 300_000L
//    }
}