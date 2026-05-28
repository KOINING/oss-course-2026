import { DEFAULT_HOME_PATH, ROUTE_NAMES } from '@/utils/constants'

export const ROLE_META = {
  admin: {
    label: '系统管理员',
    summary:
      '负责系统初始化配置、维护全局基础字典，并管理其他业务角色的账号与基础权限。',
  },
  academic_affairs: {
    label: '教务管理员',
    summary:
      '负责导入课程与班级名单、跟踪全专业计算进度，并导出认证归档所需的报表与底稿。',
  },
  program_director: {
    label: '专业负责人',
    summary:
      '负责维护毕业要求与指标点、配置课程支撑矩阵，并统一触发专业级达成度汇总计算。',
  },
  instructor: {
    label: '课程主讲教师',
    summary:
      '负责维护课程目标和考核点、导入原始成绩，并完成课程级达成度计算与自查。',
  },
}

export const PLATFORM_INTRO =
  '平台面向专业认证场景，围绕学生成绩驱动的直接评价流程，串联基础数据、课程支撑关系、原始成绩录入与达成度计算，帮助团队完成从课程级到专业级的统一分析。'

const ALL_ROLES = Object.keys(ROLE_META)

export const NAVIGATION_SECTIONS = [
  {
    key: 'home',
    label: '首页',
    icon: 'HomeFilled',
    children: [
      {
        key: 'home',
        label: '首页',
        path: DEFAULT_HOME_PATH,
        routeName: ROUTE_NAMES.HOME,
        roles: ALL_ROLES,
      },
    ],
  },
  {
    key: 'module-a',
    label: '模块 A',
    icon: 'Setting',
    children: [
      {
        key: 'basic-data',
        label: '基础数据',
        path: '/basic-data',
        routeName: ROUTE_NAMES.BASIC_DATA,
        roles: ['admin', 'academic_affairs'],
        moduleTitle: '模块 A：基础与宏观数据管理',
        summary:
          '维护学年学期、学院、专业、课程等基础主数据，为毕业要求配置和后续计算流程提供统一数据底座。',
        entities: ['College', 'Major', 'AcademicTerm', 'Course'],
      },
      {
        key: 'requirements',
        label: '毕业要求与指标点',
        path: '/requirements',
        routeName: ROUTE_NAMES.REQUIREMENTS,
        roles: ['program_director'],
        moduleTitle: '模块 A：基础与宏观数据管理',
        summary:
          '维护毕业要求主体及二级指标点，形成专业认证的目标体系，作为课程支撑矩阵配置的依据。',
        entities: ['GraduationRequirement', 'IndicatorPoint'],
      },
      {
        key: 'data-import',
        label: '数据导入',
        path: '/data-import',
        routeName: ROUTE_NAMES.DATA_IMPORT,
        roles: ['academic_affairs'],
        moduleTitle: '模块 A：基础与宏观数据管理',
        summary:
          '通过 Excel 模板批量导入全专业课程清单和教学班学生名单，支持逐行校验与错误定位。',
        entities: ['Course', 'TeachingClass', 'Student', 'StudentClass'],
      },
      {
        key: 'support-matrix',
        label: '支撑矩阵配置',
        path: '/support-matrix',
        routeName: ROUTE_NAMES.SUPPORT_MATRIX,
        roles: ['program_director'],
        moduleTitle: '模块 A：基础与宏观数据管理',
        summary:
          '配置课程与指标点之间的宏观支撑关系及总支撑权重，保证专业级达成度汇总链路完整可追踪。',
        entities: ['CourseIndicatorSupport', 'Course', 'IndicatorPoint'],
      },
    ],
  },
  {
    key: 'module-b',
    label: '模块 B',
    icon: 'Reading',
    children: [
      {
        key: 'course-objectives',
        label: '课程目标与考核点',
        path: '/course-objectives',
        routeName: ROUTE_NAMES.COURSE_OBJECTIVES,
        roles: ['instructor'],
        moduleTitle: '模块 B：课程大纲与微观映射管理',
        summary:
          '维护课程目标、课程目标对指标点的内部贡献权重，以及与考核点之间的映射关系。',
        entities: ['CourseObjective', 'ObjectiveIndicatorContribution', 'AssessmentPoint'],
      },
    ],
  },
  {
    key: 'system-admin',
    label: '系统管理',
    icon: 'UserFilled',
    children: [
      {
        key: 'account-role-management',
        label: '账号与角色管理',
        path: '/account-role-management',
        routeName: ROUTE_NAMES.ACCOUNT_ROLE_MANAGEMENT,
        roles: ['admin'],
        moduleTitle: '系统管理',
        summary:
          '系统管理员用于管理教务管理员、专业负责人和课程主讲教师的账号，完成账号启停、角色分配与基础权限维护。',
        entities: ['sys_user', 'sys_role', 'sys_user_role', 'sys_role_permission'],
        componentKey: 'account-role-management',
      },
      {
        key: 'component-demo',
        label: '组件预览（开发）',
        path: '/component-demo',
        routeName: ROUTE_NAMES.COMPONENT_DEMO,
        roles: ['admin'],
        moduleTitle: '系统管理',
        summary:
          '页面组 C 前端组件预览与调试页面（开发期间使用，上线前移除）。',
        entities: [],
      },
    ],
  },
  {
    key: 'module-c',
    label: '模块 C',
    icon: 'DataAnalysis',
    children: [
      {
        key: 'assessment',
        label: '成绩录入与计算',
        path: '/assessment',
        routeName: ROUTE_NAMES.ASSESSMENT,
        roles: ['academic_affairs', 'program_director', 'instructor'],
        moduleTitle: '模块 C：成绩录入与直接评价',
        summary:
          '承接班级与学生数据、原始成绩导入、课程级计算和专业级汇总，是平台达成度分析的执行中心。',
        entities: [
          'TeachingClass',
          'Student',
          'StudentClass',
          'StudentAssessmentScore',
          'CourseObjectiveAchievement',
          'CourseIndicatorAchievement',
          'MajorIndicatorAchievement',
        ],
      },
    ],
  },
  {
    key: 'module-d',
    label: '模块 D',
    icon: 'Document',
    children: [
      {
        key: 'reports',
        label: '报表与底稿',
        path: '/reports',
        routeName: ROUTE_NAMES.REPORTS,
        roles: ['academic_affairs', 'program_director', 'instructor'],
        moduleTitle: '模块 D：报表生成与底稿导出',
        summary:
          '聚合课程级和专业级的计算结果，生成课程评价表、专业分析报表以及可用于归档与抽查的穿透式底稿。',
        entities: [
          'CourseObjectiveAchievement',
          'CourseIndicatorAchievement',
          'MajorIndicatorAchievement',
        ],
      },
    ],
  },
]

export function getRoleDetails(roleCodes = []) {
  return roleCodes
    .map((roleCode) => ({
      code: roleCode,
      ...ROLE_META[roleCode],
    }))
    .filter((role) => role.label)
}

export function getVisibleSections(roleCodes = []) {
  const roleSet = new Set(roleCodes)

  return NAVIGATION_SECTIONS.map((section) => ({
    ...section,
    children:
      section.key === 'home'
        ? section.children
        : section.children.filter((item) =>
            item.roles.some((roleCode) => roleSet.has(roleCode)),
          ),
  })).filter((section) => section.children.length > 0)
}

export function getProtectedRoutes() {
  return NAVIGATION_SECTIONS.flatMap((section) =>
    section.children.map((item) => ({
      ...item,
      sectionKey: section.key,
      sectionLabel: section.label,
      icon: section.icon,
    })),
  )
}
