CREATE TABLE IF NOT EXISTS `portal` (
    `id` bigint NOT NULL COMMENT '主键ID',
    `name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
    `is_active` int NOT NULL COMMENT '是否启用',
    `config` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置',
    `sort` int NOT NULL COMMENT '排序号',
    `module_group` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模块组',
    `type` enum('global','personal') COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型',
    `fcu` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人',
    `fcd` timestamp(3) NOT NULL COMMENT '创建时间',
    `lcu` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '修改人',
    `lcd` timestamp(3) NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门户配置';

CREATE TABLE IF NOT EXISTS `portal_authority` (
    `portal_id` bigint NOT NULL COMMENT '门户ID',
    `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象',
    `type` enum('common','user','team','role') COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象类型',
    PRIMARY KEY (`portal_id`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门户配置使用授权对象';

CREATE TABLE IF NOT EXISTS `portal_widget_authority` (
    `portal_widget_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '门户小部件名称',
    `uuid` char(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象',
    `type` enum('common','user','team','role') COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象类型',
    PRIMARY KEY (`portal_widget_name`,`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='门户小部件使用授权对象';

CREATE TABLE IF NOT EXISTS `portal_user` (
    `user_uuid` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户UUID',
    `module_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模块组',
    `portal_id` bigint NOT NULL COMMENT '门户配置ID',
    PRIMARY KEY (`user_uuid`,`module_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户选择的门户配置';
