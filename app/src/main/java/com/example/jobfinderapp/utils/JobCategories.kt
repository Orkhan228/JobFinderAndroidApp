package com.example.jobfinderapp.utils

import com.example.jobfinderapp.entity.Category

object JobCategories {

    val categoriesList = listOf<Category>(
        Category("Accounting & Finance Jobs", "accounting-finance-jobs"),
        Category("IT Jobs", "it-jobs"),
        Category("Sales Jobs", "sales-jobs"),
        Category("Customer Services Jobs", "customer-services-jobs"),
        Category("Engineering Jobs", "engineering-jobs"),
        Category("HR & Recruitment Jobs", "hr-jobs"),
        Category("Healthcare & Nursing Jobs", "healthcare-nursing-jobs"),
        Category("Hospitality & Catering Jobs", "hospitality-catering-jobs"),
        Category("PR, Advertising & Marketing Jobs", "pr-advertising-marketing-jobs"),
        Category("Logistics & Warehouse Jobs", "logistics-warehouse-jobs"),
        Category("Teaching Jobs", "teaching-jobs"),
        Category("Admin Jobs", "admin-jobs"),
        Category("Creative & Design Jobs", "creative-design-jobs"),
        Category("Scientific & QA Jobs", "scientific-qa-jobs"),
        Category("Social work Jobs", "social-work-jobs"),
    )
}