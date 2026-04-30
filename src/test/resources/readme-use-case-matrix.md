# Матрица покрытия use case из README

## Системные UI-тесты Selenium / HtmlUnit

Системными считаются только те тесты, которые выполняют сценарий через видимые страницы, ссылки, кнопки, формы и фильтры приложения. DAO-хелперы используются только для подготовки данных и проверки постусловий.

| Use case | UI-маршрут | Тесты |
| --- | --- | --- |
| 01 Просмотреть список людей | `P-01 -> P-02` или прямой вход на `P-02` как пользовательскую точку входа | `HomeAndNavigationSystemTests.homePageShouldNavigateToPeopleAndCompaniesSections`, `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 02 Открыть карточку человека | `P-02 -> P-03` | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 03 Добавить нового человека | `P-02 -> P-04 -> submit` | `PeopleSystemTests.createPersonShouldSaveValidPerson` |
| 04 Отредактировать данные человека | `P-02 -> P-03 -> P-04(edit) -> submit` | `PeopleSystemTests.editPersonShouldUpdateExistingPerson` |
| 05 Удалить человека | `P-02 -> P-03 -> delete` | `PeopleSystemTests.personCardShouldDeletePersonAndReturnToList` |
| 06 Просмотреть историю работы человека | `P-02 -> P-03` | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard`, `WorkExperienceSystemTests.personCardShouldShowEmptyHistoryStateAndOpenCreateForm` |
| 07 Добавить запись в историю работы | `P-02 -> P-03 -> P-05(create) -> submit` | `WorkExperienceSystemTests.workExperienceCreateShouldSaveValidRecord` |
| 08 Отредактировать запись о месте работы | `P-02 -> P-03 -> P-05(edit) -> submit` | `WorkExperienceSystemTests.workExperienceEditShouldUpdateRecord` |
| 09 Удалить запись из истории работы | `P-02 -> P-03 -> P-05(edit) -> delete` | `WorkExperienceSystemTests.workExperienceEditShouldDeleteRecord` |
| 10 Просмотреть список компаний | `P-01 -> P-06` или прямой вход на `P-06` как пользовательскую точку входа | `HomeAndNavigationSystemTests.homePageShouldNavigateToPeopleAndCompaniesSections`, `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 11 Открыть карточку компании | `P-06 -> P-07` | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 12 Добавить новую компанию | `P-06 -> P-08(create) -> submit` | `CompaniesSystemTests.companyCreateShouldSaveValidCompany` |
| 13 Отредактировать данные компании | `P-06 -> P-07 -> P-08(edit) -> submit` | `CompaniesSystemTests.companyEditShouldUpdateExistingCompany` |
| 14 Удалить компанию | `P-06 -> P-07 -> delete` | `CompaniesSystemTests.companyCardShouldBlockDeletionWhenReferenced`, `CompaniesSystemTests.companyCardShouldDeleteFreeCompany` |
| 15 Просмотреть список вакансий компании | `P-06 -> P-07` | `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive`, `CompaniesSystemTests.companyCardShouldShowNoVacanciesState`, `CompaniesSystemTests.companyCardShouldShowNoMatchingVacanciesState` |
| 16 Добавить вакансию в компанию | `P-06 -> P-07 -> P-10(create) -> submit` | `VacanciesSystemTests.vacancyCreateShouldSaveValidVacancy` |
| 17 Отредактировать вакансию | `P-06 -> P-07 -> P-09 -> P-10(edit) -> submit` | `VacanciesSystemTests.vacancyCardShouldOpenAndAllowEditingData` |
| 18 Удалить вакансию | `P-06 -> P-07 -> P-09 -> delete` | `VacanciesSystemTests.vacancyCardShouldDeleteVacancyAndReturnToCompany` |
| 19 Просмотреть подробности вакансии | `P-06 -> P-07 -> P-09` | `VacanciesSystemTests.vacancyCardShouldOpenAndAllowEditingData` |
| 20 Закрыть вакансию без удаления | `P-06 -> P-07 -> P-09 -> close` | `VacanciesSystemTests.vacancyCardShouldCloseVacancy` |
| 21 Переоткрыть ранее закрытую вакансию | `P-06 -> P-07 -> P-09 -> reopen` | `VacanciesSystemTests.vacancyCardShouldReopenVacancy` |
| 22 Показывать в поиске только активные вакансии | `P-06 -> filters`, `P-07 -> filters`, `P-03 -> P-11` | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard`, `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive`, `MatchingSystemTests.personMatchesShouldHideClosedVacanciesAndShowBlankEducationInfoThroughUi` |
| 23 Найти резюме по образованию | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldFilterByEducationAndOpenPersonCard` |
| 24 Найти резюме по зарплате | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 25 Найти резюме по компаниям, в которых человек работал | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 26 Найти резюме по должностям, которые человек занимал | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 27 Найти вакансии по компании | `P-06 -> P-07` | `CompaniesSystemTests.companiesListShouldFilterByNameAndOpenVacanciesAndOpenCard` |
| 28 Найти вакансии по должности | `P-06 -> filters`, `P-09 -> link -> P-07(filtered)` | `CompaniesSystemTests.companiesListShouldFilterByVacancyCriteriaThroughForm`, `VacanciesSystemTests.vacancyCardShouldFindVacanciesBySamePositionLink` |
| 29 Найти вакансии по зарплате | `P-06 -> filters`, `P-07 -> filters` | `CompaniesSystemTests.companiesListShouldFilterByVacancyCriteriaThroughForm`, `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 30 Применить несколько фильтров к списку резюме | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 31 Применить несколько фильтров к списку вакансий | `P-07 -> filters` | `CompaniesSystemTests.companyCardShouldFilterVacanciesByPositionSalaryAndOnlyActive` |
| 32 Отсортировать результаты по зарплате | `P-02 -> filters` | `PeopleSystemTests.peopleListShouldApplyCombinedFiltersAndSortBySalary` |
| 33 Скрывать из подбора людей, которые сейчас не ищут работу | `P-06 -> P-07 -> P-09 -> P-11` | `MatchingSystemTests.vacancyMatchesShouldShowClosedVacancyAndBlankEducationInfoThroughUi` |
| 34 Скрывать закрытые вакансии | `P-03 -> P-11` | `MatchingSystemTests.personMatchesShouldHideClosedVacanciesAndShowBlankEducationInfoThroughUi` |
| 35 Найти подходящие вакансии для выбранного человека | `P-02 -> P-03 -> P-11` | `MatchingSystemTests.personCardShouldOpenMatchesPage`, `MatchingSystemTests.personMatchesShouldHideClosedVacanciesAndShowBlankEducationInfoThroughUi`, `MatchingSystemTests.personMatchesShouldShowEmptyStateWhenNoVacanciesMatch` |
| 36 Найти подходящие резюме для выбранной вакансии | `P-06 -> P-07 -> P-09 -> P-11` | `MatchingSystemTests.vacancyCardShouldOpenMatchesPage`, `MatchingSystemTests.vacancyMatchesShouldShowClosedVacancyAndBlankEducationInfoThroughUi`, `MatchingSystemTests.vacancyMatchesShouldShowEmptyStateWhenNoPeopleMatch` |
| 37 Отсортировать список совпадений | `P-03 -> P-11 -> filters`, `P-09 -> P-11 -> filters` | `MatchingSystemTests.personMatchesShouldSortVisibleVacanciesThroughForm`, `MatchingSystemTests.vacancyMatchesShouldSortVisiblePeopleThroughForm` |
| 38 Проверить обязательные поля вакансии | `P-06 -> P-07 -> P-10(create) -> submit` | `VacanciesSystemTests.vacancyCreateShouldValidateRequiredFields` |
| 39 Проверить обязательные поля человека | `P-02 -> P-04(create) -> submit` | `PeopleSystemTests.createPersonShouldShowValidationErrors` |
| 40 Проверить отсутствие пересечений дат в истории работы | `P-02 -> P-03 -> P-05(create) -> submit` | `WorkExperienceSystemTests.workExperienceCreateShouldRejectOverlappingPeriod` |

## Дополнительное Selenium-покрытие пользовательски достижимых UI-ошибок

Эти сценарии не добавляют новых use case из README, но закрывают отдельные пользовательские ветки ошибок и валидации, доступные через обычные элементы интерфейса.

| UI-ветка | Пользовательский маршрут | Тест |
| --- | --- | --- |
| Невалидный диапазон зарплаты на списке компаний | `P-06 -> filters` | `CompaniesSystemTests.companiesListShouldShowValidationErrorWhenSalaryRangeIsInvalid` |
| Невалидный диапазон зарплаты на карточке компании | `P-07 -> vacancy filters` | `CompaniesSystemTests.companyCardShouldShowValidationErrorWhenVacancySalaryRangeIsInvalid` |
| Дубликат названия при редактировании компании | `P-06 -> P-07 -> P-08(edit) -> submit` | `CompaniesSystemTests.companyEditShouldRejectDuplicateNameAndKeepOriginalData` |
| Пустое/слишком длинное значение при редактировании компании | `P-06 -> P-07 -> P-08(edit) -> submit` | `CompaniesSystemTests.companyEditShouldRejectBlankAndTooLongValuesAndKeepOriginalData` |
| Невалидное редактирование вакансии | `P-06 -> P-07 -> P-09 -> P-10(edit) -> submit` | `VacanciesSystemTests.vacancyEditShouldRejectInvalidUpdateAndKeepOriginalData` |
| Пересечение дат при редактировании записи о работе | `P-02 -> P-03 -> P-05(edit) -> submit` | `WorkExperienceSystemTests.workExperienceEditShouldRejectOverlappingPeriodAndKeepOriginalData` |
| Полевая валидация при редактировании записи о работе | `P-02 -> P-03 -> P-05(edit) -> submit` | `WorkExperienceSystemTests.workExperienceEditShouldRejectInvalidRangeAndNegativeSalary` |

## Дополнительные integration/HTTP-тесты для веток вне честного UI-сценария

Эти проверки не считаются системными UI-тестами, потому что пользователь не может честно сгенерировать такие запросы через обычные элементы интерфейса.

| Сценарий | Почему не Selenium | Тест |
| --- | --- | --- |
| Прямой POST с недопустимым образованием | В UI поле ограничено `select` | `WebHttpIntegrationTests.createPersonShouldRejectInvalidEducationOnDirectPost` |
| Подмена `companyId` при создании вакансии | В UI компания фиксируется карточкой компании | `WebHttpIntegrationTests.vacancyCreateShouldRejectCompanyBindingMismatchOnDirectPost` |
| Невалидные query-параметры списка компаний | Формы UI не дают отправить `abc` вместо числа или `oops` вместо boolean | `WebHttpIntegrationTests.companiesListShouldReportInvalidVacancyFiltersOnDirectGet` |
| Невалидные query-параметры фильтров вакансий компании | Формы UI не дают отправить невалидные типы | `WebHttpIntegrationTests.companyCardShouldReportInvalidVacancyFiltersOnDirectGet` |
| Невалидные query-параметры подбора для человека | Формы UI не дают отправить произвольные значения `sort/onlyActive` | `WebHttpIntegrationTests.personMatchesShouldReportInvalidQueryParamsOnDirectGet` |
| Явный `onlyActive=false` в подборе вакансий | Текущая checkbox-форма не может честно отправить `false` как отдельное значение | `WebHttpIntegrationTests.personMatchesShouldAllowClosedVacanciesOnDirectGetWhenExplicitlyRequested` |
| Невалидные query-параметры подбора для вакансии | Формы UI не дают отправить произвольные значения `sort/onlyLookingForJob` | `WebHttpIntegrationTests.vacancyMatchesShouldReportInvalidQueryParamsOnDirectGet` |
| Явный `onlyLookingForJob=false` в подборе резюме | Текущая checkbox-форма не может честно отправить `false` как отдельное значение | `WebHttpIntegrationTests.vacancyMatchesShouldAllowPeopleNotLookingForJobOnDirectGetWhenExplicitlyRequested` |
| Friendly error page со статусом `500` | Это проверка технического HTTP-статуса и страницы ошибки | `WebHttpIntegrationTests.unexpectedErrorsShouldRenderFriendlyPageWith500Status` |
