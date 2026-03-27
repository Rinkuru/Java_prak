TRUNCATE work_experience, vacancy, person, company RESTART IDENTITY CASCADE;

INSERT INTO company (name, description) VALUES
  ('ВКонтакте', 'Крупнейшая российская социальная сеть и IT-компания'),
  ('Максимед', 'Сеть многопрофильных медицинских центров в Москве'),
  ('1С', 'Российский разработчик программного обеспечения для автоматизации бизнеса');


INSERT INTO vacancy (company_id, position, salary, required_education, requirements, status) VALUES
  (1, 'Backend-разработчик',  240000, 'Высшее техническое',   'Опыт коммерческой backend-разработки от 3 лет, знание SQL и микросервисной архитектуры', TRUE),
  (1, 'Frontend-разработчик', 210000, 'Высшее техническое',   'Опыт frontend-разработки от 2 лет, уверенное владение JavaScript и React', TRUE),
  (1, 'Golang-разработчик',   260000, 'Высшее техническое',   'Опыт разработки на Go от 3 лет, работа с высоконагруженными сервисами', TRUE),
  (1, 'Продакт-менеджер',     200000, 'Высшее',               'Опыт управления цифровым продуктом от 2 лет, работа с аналитикой и roadmap', FALSE);

INSERT INTO vacancy (company_id, position, salary, required_education, requirements, status) VALUES
  (2, 'Терапевт',                    110000, 'Высшее медицинское',  'Опыт амбулаторного приёма от 2 лет, действующий сертификат специалиста', TRUE),
  (2, 'Ортопед',                     130000, 'Высшее медицинское',  'Опыт ортопедической практики от 3 лет, ведение медицинской документации', TRUE),
  (2, 'Продавец-консультант',         60000, 'Среднее специальное', 'Опыт работы в рознице от 1 года, навыки консультирования клиентов', TRUE),
  (2, 'Специалист по учёту товаров',  65000, 'Среднее специальное', 'Опыт складского учёта от 1 года, уверенное владение Excel и 1С', FALSE);

INSERT INTO vacancy (company_id, position, salary, required_education, requirements, status) VALUES
  (3, 'Разработчик 1С',                   160000, 'Высшее техническое', 'Опыт разработки на платформе 1С от 2 лет, знание типовых конфигураций', TRUE),
  (3, 'Тестировщик',                      120000, 'Высшее техническое', 'Опыт функционального тестирования от 1 года, написание тест-кейсов и баг-репортов', TRUE),
  (3, 'Менеджер проектов',                140000, 'Высшее',             'Опыт управления ИТ-проектами от 2 лет, взаимодействие с заказчиками и командой', TRUE),
  (3, 'Разработчик кассового ПО',         180000, 'Высшее техническое', 'Опыт разработки бизнес-приложений от 2 лет, понимание интеграции с торговым оборудованием', FALSE);



INSERT INTO person (full_name, home_address, education, status, desired_position, desired_salary) VALUES
  -- 1
  ('Иванов Алексей Дмитриевич',    'Москва, Университетский проспект, д. 5, кв. 12',         'Высшее техническое',  TRUE,  'Backend-разработчик',     230000),
  -- 2
  ('Смирнова Ольга Игоревна',      'Москва, Воробьёвы горы, д. 1, корп. 2, кв. 45',          'Высшее медицинское',  TRUE,  'Терапевт',                105000),
  -- 3
  ('Кузнецов Павел Сергеевич',     'Москва, Ломоносовский проспект, д. 14, кв. 88',           'Высшее техническое', FALSE, NULL,                      NULL),
  -- 4
  ('Попова Наталья Андреевна',     'Москва, Москва-Сити, Пресненская набережная, д. 8, кв. 210', 'Высшее',          TRUE,  'Продакт-менеджер',        190000),
  -- 5
  ('Соколов Иван Николаевич',      'Москва, улица Косыгина, д. 7, кв. 3',                     'Высшее техническое',  TRUE,  'Golang-разработчик',     250000),
  -- 6
  ('Лебедева Татьяна Владимировна','Москва, Воробьёвы горы, д. 2, корп. 1, кв. 18',           'Высшее медицинское',  FALSE, NULL,                     NULL),
  -- 7
  ('Новиков Артём Олегович',       'Москва, Москва-Сити, Башня Федерация, кв. 55',             'Высшее техническое', TRUE,  'Разработчик 1С',         155000),
  -- 8
  ('Морозова Екатерина Петровна',  'Москва, улица Вавилова, д. 42, кв. 7',                    'Среднее специальное', TRUE,  'Продавец-консультант',   55000),
  -- 9
  ('Волков Михаил Романович',      'Москва, Ломоносовский проспект, д. 22, кв. 60',            'Высшее техническое', TRUE,  'Frontend-разработчик',   200000),
  -- 10
  ('Алексеева Дарья Юрьевна',      'Москва, Москва-Сити, Пресненская набережная, д. 10, кв. 99', 'Высшее',           TRUE,  'Менеджер проектов',      130000),
  -- 11
  ('Зайцев Дмитрий Константинович','Москва, улица Косыгина, д. 15, кв. 101',                  'Высшее техническое',  FALSE, NULL,                     NULL),
  -- 12
  ('Павлова Анна Сергеевна',       'Москва, Воробьёвы горы, д. 3, кв. 29',                    'Высшее медицинское',  TRUE,  'Ортопед',                125000),
  -- 13
  ('Семёнов Роман Васильевич',     'Москва, улица Вавилова, д. 18, кв. 4',                    'Высшее техническое',  TRUE, 'Разработчик кассового ПО', 170000),
  -- 14
  ('Козлова Ирина Александровна',  'Москва, Университетский проспект, д. 9, кв. 56',           'Среднее специальное',FALSE, NULL,                     NULL),
  -- 15
  ('Фёдоров Андрей Евгеньевич',   'Москва, Москва-Сити, Башня Эволюция, кв. 143',             'Высшее техническое',  TRUE,  'Тестировщик',            115000),
  -- 16
  ('Орлова Мария Ивановна',        'Москва, Ломоносовский проспект, д. 6, кв. 33',             'Высшее',             TRUE,  'Продакт-менеджер',       185000),
  -- 17
  ('Михайлов Игорь Леонидович',    'Москва, улица Косыгина, д. 3, кв. 77',                    'Высшее техническое',  FALSE, NULL,                     NULL),
  -- 18
  ('Андреева Светлана Борисовна',  'Москва, Воробьёвы горы, д. 4, корп. 3, кв. 11',           'Высшее медицинское',  TRUE,  'Терапевт',               100000),
  -- 19
  ('Яковлев Николай Фёдорович',   'Москва, Москва-Сити, Пресненская набережная, д. 12, кв. 74', 'Высшее техническое',TRUE, 'Backend-разработчик',     220000),
  -- 20
  ('Степанова Юлия Максимовна',    'Москва, улица Вавилова, д. 31, кв. 19',                   'Среднее специальное', TRUE,  'Специалист по учёту',    60000),
  -- 21
  ('Николаев Сергей Александрович','Москва, Университетский проспект, д. 17, кв. 2',           'Высшее техническое', FALSE, NULL,                     NULL);


-- Иванов Алексей (person_id=1): работал в 1С, сейчас в ВКонтакте
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (1, 3, 'Разработчик 1С',      130000, '2018-07-01', '2021-03-31'),
  (1, 1, 'Backend-разработчик', 210000, '2021-04-01', NULL);

-- Смирнова Ольга (person_id=2): работала в Максимед
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (2, 2, 'Терапевт', 90000, '2019-09-01', '2024-01-31');

-- Соколов Иван (person_id=5): работал в ВКонтакте
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (5, 1, 'Backend-разработчик', 190000, '2020-02-01', '2023-06-30');

-- Новиков Артём (person_id=7): работал в 1С, потом в ВКонтакте
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (7, 3, 'Тестировщик',         100000, '2017-05-01', '2020-08-31'),
  (7, 1, 'Backend-разработчик', 180000, '2020-09-01', '2023-12-31');

-- Волков Михаил (person_id=9): работал в ВКонтакте
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (9, 1, 'Frontend-разработчик', 175000, '2021-01-01', NULL);

-- Алексеева Дарья (person_id=10): работала в 1С
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (10, 3, 'Менеджер проектов', 120000, '2019-03-01', '2023-11-30');

-- Павлова Анна (person_id=12): работает в Максимед
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (12, 2, 'Ортопед', 115000, '2020-06-01', NULL);

-- Семёнов Роман (person_id=13): работал в 1С
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (13, 3, 'Разработчик кассового ПО', 155000, '2018-11-01', NULL);

-- Андреева Светлана (person_id=18): работала в Максимед
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (18, 2, 'Терапевт', 85000, '2016-04-01', '2022-09-30');

-- Яковлев Николай (person_id=19): работал в 1С, потом в ВКонтакте
INSERT INTO work_experience (person_id, company_id, position, salary, start_date, end_date) VALUES
  (19, 3, 'Разработчик 1С',      140000, '2017-09-01', '2020-05-31'),
  (19, 1, 'Backend-разработчик', 200000, '2020-06-01', NULL);


-- Индексы для ускорения поиска и подбора
CREATE INDEX IF NOT EXISTS idx_person_full_name ON person (full_name);
CREATE INDEX IF NOT EXISTS idx_person_education ON person (education);
CREATE INDEX IF NOT EXISTS idx_person_status ON person (status);
CREATE INDEX IF NOT EXISTS idx_person_desired_salary ON person (desired_salary);

CREATE INDEX IF NOT EXISTS idx_company_name ON company (name);

CREATE INDEX IF NOT EXISTS idx_vacancy_company_id ON vacancy (company_id);
CREATE INDEX IF NOT EXISTS idx_vacancy_position ON vacancy (position);
CREATE INDEX IF NOT EXISTS idx_vacancy_salary ON vacancy (salary);
CREATE INDEX IF NOT EXISTS idx_vacancy_status ON vacancy (status);
CREATE INDEX IF NOT EXISTS idx_vacancy_required_education ON vacancy (required_education);

CREATE INDEX IF NOT EXISTS idx_workexp_person_id ON work_experience (person_id);
CREATE INDEX IF NOT EXISTS idx_workexp_company_id ON work_experience (company_id);
CREATE INDEX IF NOT EXISTS idx_workexp_position ON work_experience (position);
CREATE INDEX IF NOT EXISTS idx_workexp_start_date ON work_experience (start_date);
CREATE INDEX IF NOT EXISTS idx_workexp_end_date ON work_experience (end_date);
