# Матрица покрытия use case из README

## Selenium / HtmlUnit

| Use case | Тесты |
| --- | --- |
| 01 Просмотреть список людей | `HomeAndNavigationSystemTests.homePageShouldNavigateToPeopleAndCompaniesSections`, `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 02 Открыть карточку человека | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 03 Добавить нового человека | `PeopleSystemTests.createPersonShouldSaveValidPerson` |
| 04 Отредактировать данные человека | `PeopleSystemTests.editPersonShouldUpdateExistingPerson` |
| 05 Удалить человека | `PeopleSystemTests.personCardShouldDeletePersonAndReturnToList` |
| 06 Просмотреть историю работы человека | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard`, `WorkExperienceSystemTests.personCardShouldShowEmptyHistoryStateAndOpenCreateForm` |
| 07 Добавить запись в историю работы | `WorkExperienceSystemTests.workExperienceFormsShouldValidateOverlapUpdateAndDelete` |
| 08 Отредактировать запись о месте работы | `WorkExperienceSystemTests.workExperienceFormsShouldValidateOverlapUpdateAndDelete` |
| 09 Удалить запись из истории работы | `WorkExperienceSystemTests.workExperienceFormsShouldValidateOverlapUpdateAndDelete` |
| 10 Просмотреть список компаний | `HomeAndNavigationSystemTests.homePageShouldNavigateToPeopleAndCompaniesSections`, `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 11 Открыть карточку компании | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 12 Добавить новую компанию | `CompaniesSystemTests.companyCreateShouldSaveValidCompany` |
| 13 Отредактировать данные компании | `CompaniesSystemTests.companyFormsShouldValidateRejectDuplicateAndTooLongFieldsAndUpdateData` |
| 14 Удалить компанию | `CompaniesSystemTests.companyCardShouldBlockDeletionWhenReferencedAndDeleteFreeCompany` |
| 15 Просмотреть список вакансий компании | `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive`, `CompaniesSystemTests.companyCardShouldShowNoVacanciesAndNoMatchingStates` |
| 16 Добавить вакансию в компанию | `VacanciesSystemTests.vacancyLifecycleShouldValidateCreateCloseReopenAndDelete` |
| 17 Отредактировать вакансию | `VacanciesSystemTests.vacancyCardShouldOpenAndAllowEditingData` |
| 18 Удалить вакансию | `VacanciesSystemTests.vacancyLifecycleShouldValidateCreateCloseReopenAndDelete` |
| 19 Просмотреть подробности вакансии | `VacanciesSystemTests.vacancyCardShouldOpenAndAllowEditingData` |
| 20 Закрыть вакансию без удаления | `VacanciesSystemTests.vacancyLifecycleShouldValidateCreateCloseReopenAndDelete` |
| 21 Переоткрыть ранее закрытую вакансию | `VacanciesSystemTests.vacancyLifecycleShouldValidateCreateCloseReopenAndDelete` |
| 22 Показывать в поиске только активные вакансии | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard`, `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 23 Найти резюме по образованию | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 24 Найти резюме по зарплате | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 25 Найти резюме по компаниям, в которых человек работал | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 26 Найти резюме по должностям, которые человек занимал | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 27 Найти вакансии по компании | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 28 Найти вакансии по должности | `VacanciesSystemTests.vacancyCardShouldFindVacanciesBySamePositionLink`, `CompaniesSystemTests.companiesListShouldValidateVacancyFilterParamsAndKeepSearchWorking` |
| 29 Найти вакансии по зарплате | `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 30 Применить несколько фильтров к списку резюме | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 31 Применить несколько фильтров к списку вакансий | `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 32 Отсортировать результаты по зарплате | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 33 Скрывать из подбора людей, которые сейчас не ищут работу | `MatchingSystemTests.vacancyMatchesShouldHandleClosedVacancyBlankEducationFiltersAndSorting` |
| 34 Скрывать закрытые вакансии | `MatchingSystemTests.personMatchesShouldRespectOnlyActiveSortAndBlankEducationState`, `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 35 Найти подходящие вакансии для выбранного человека | `MatchingSystemTests.personCardShouldOpenMatchesPage`, `MatchingSystemTests.personMatchesShouldRespectOnlyActiveSortAndBlankEducationState`, `MatchingSystemTests.personMatchesShouldShowEmptyStateWhenNoVacanciesMatch` |
| 36 Найти подходящие резюме для выбранной вакансии | `MatchingSystemTests.vacancyCardShouldOpenMatchesPage`, `MatchingSystemTests.vacancyMatchesShouldHandleClosedVacancyBlankEducationFiltersAndSorting`, `MatchingSystemTests.vacancyMatchesShouldShowEmptyStateWhenNoPeopleMatch` |
| 37 Отсортировать список совпадений | `MatchingSystemTests.personMatchesShouldRespectOnlyActiveSortAndBlankEducationState`, `MatchingSystemTests.vacancyMatchesShouldHandleClosedVacancyBlankEducationFiltersAndSorting` |
| 38 Проверить обязательные поля вакансии | `VacanciesSystemTests.vacancyLifecycleShouldValidateCreateCloseReopenAndDelete` |
| 39 Проверить обязательные поля человека | `PeopleSystemTests.createPersonShouldShowValidationErrors` |
| 40 Проверить отсутствие пересечений дат в истории работы | `WorkExperienceSystemTests.workExperienceFormsShouldValidateOverlapUpdateAndDelete` |

## HTTP / integration для веток вне честного UI-сценария

| Сценарий | Тест |
| --- | --- |
| Прямой POST с недопустимым образованием | `WebHttpIntegrationTests.createPersonShouldRejectInvalidEducationOnDirectPost` |
| Подмена `companyId` при создании вакансии | `WebHttpIntegrationTests.vacancyCreateShouldRejectCompanyBindingMismatchOnDirectPost` |
| Friendly error page со статусом `500` | `WebHttpIntegrationTests.unexpectedErrorsShouldRenderFriendlyPageWith500Status` |
