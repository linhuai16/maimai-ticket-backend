/*
SQLyog Ultimate v13.1.1 (64 bit)
MySQL - 5.7.26 : Database - maimai_ticket
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`maimai_ticket` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `maimai_ticket`;

/*Table structure for table `admin_account` */

DROP TABLE IF EXISTS `admin_account`;

CREATE TABLE `admin_account` (
  `admin_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '管理员主键',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '后台登录账号',
  `password_hash` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PBKDF2 密码哈希',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员昵称',
  `account_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `uk_admin_account_username` (`username`),
  KEY `idx_admin_account_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员账号';

/*Data for the table `admin_account` */

/*Table structure for table `audience` */

DROP TABLE IF EXISTS `audience`;

CREATE TABLE `audience` (
  `audience_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '观演人主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '所属用户ID',
  `real_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `certificate_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件类型',
  `certificate_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号，加密存储预留',
  `certificate_no_hash` char(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号哈希，用于查重',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`audience_id`),
  UNIQUE KEY `uk_audience_user_cert` (`user_id`,`certificate_no_hash`),
  KEY `idx_audience_user_default` (`user_id`,`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观演人主数据';

/*Data for the table `audience` */

/*Table structure for table `banner_item` */

DROP TABLE IF EXISTS `banner_item`;

CREATE TABLE `banner_item` (
  `banner_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Banner主键',
  `banner_title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '后台识别标题',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片地址/资源名',
  `target_project_id` bigint(20) unsigned NOT NULL COMMENT '目标项目，必填',
  `target_session_id` bigint(20) unsigned DEFAULT NULL COMMENT '目标场次，可空',
  `enable_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '启用状态',
  `sort_order` int(11) NOT NULL COMMENT '排序',
  `start_time` datetime NOT NULL COMMENT '生效时间',
  `end_time` datetime NOT NULL COMMENT '失效时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`banner_id`),
  KEY `idx_banner_effective` (`enable_status`,`start_time`,`end_time`,`sort_order`),
  KEY `idx_banner_target_project` (`target_project_id`),
  KEY `idx_banner_target_session` (`target_session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页Banner配置';

/*Data for the table `banner_item` */

insert  into `banner_item`(`banner_id`,`banner_title`,`image_url`,`target_project_id`,`target_session_id`,`enable_status`,`sort_order`,`start_time`,`end_time`,`create_time`,`update_time`) values 
(7,'星河音乐节购票季','/media/banner/image/2026/08/20260824110311_b8a7ddc2_四方图.jpg',1015,NULL,'ENABLED',1,'2026-08-01 00:00:00','2026-12-31 23:59:59','2026-08-12 17:47:03','2026-08-24 11:03:14');
insert  into `banner_item`(`banner_id`,`banner_title`,`image_url`,`target_project_id`,`target_session_id`,`enable_status`,`sort_order`,`start_time`,`end_time`,`create_time`,`update_time`) values 
(8,'纸上星河·北京站','/media/banner/image/2026/08/20260824110013_061a9004_1731068382110_nEY2.jpg',190003,190003,'ENABLED',2,'2026-08-01 00:00:00','2026-09-05 18:30:00','2026-08-12 23:04:51','2026-08-24 11:00:15');

/*Table structure for table `category` */

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `category_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '分类主键',
  `category_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
  `category_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `icon_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类图标资源名/地址',
  `sort_order` int(10) unsigned NOT NULL COMMENT '展示排序',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  UNIQUE KEY `uk_category_name` (`category_name`),
  KEY `idx_category_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出分类配置';

/*Data for the table `category` */

insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(1,'CONCERT','演唱会','yanchanghui',1);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(2,'DRAMA','话剧','huajugeju',2);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(3,'MUSIC_FESTIVAL','音乐节','yinlejie',3);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(4,'TALKSHOW','脱口秀','tuokouxiu',4);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(5,'IMMERSIVE','沉浸剧场','hongsejuchang',5);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(6,'CROSSTALK','相声','xiangsheng',6);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(7,'OPERA','戏曲艺术','xiqu',7);
insert  into `category`(`category_id`,`category_code`,`category_name`,`icon_url`,`sort_order`) values 
(8,'DANCE','舞蹈舞剧','wudao',8);

/*Table structure for table `electronic_ticket` */

DROP TABLE IF EXISTS `electronic_ticket`;

CREATE TABLE `electronic_ticket` (
  `ticket_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '电子票主键',
  `ticket_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '本系统电子票编号',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '所属订单ID',
  `order_item_id` bigint(20) unsigned NOT NULL COMMENT '所属订单项ID',
  `order_audience_id` bigint(20) unsigned NOT NULL COMMENT '绑定的订单观演人快照ID',
  `source_provider_id` bigint(20) unsigned DEFAULT NULL COMMENT '第三方票源ID',
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方订单ID',
  `provider_ticket_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方单票ID',
  `provider_ticket_product_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'V1.2第三方逐票票档ID，订单内必须唯一',
  `ticket_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '电子票状态',
  `credential_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'QR_CODE/BARCODE/TEXT/URL/DYNAMIC_QR',
  `dynamic_qr_mode` varchar(24) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'REMOTE_REFRESH/LOCAL_CRYPTO/PROVIDER_WALLET',
  `credential_payload` longtext COLLATE utf8mb4_unicode_ci COMMENT '第三方电子凭证原始内容',
  `credential_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方凭证版本',
  `credential_expire_time` datetime DEFAULT NULL COMMENT '当前动态凭证过期时间',
  `refresh_after_seconds` int(10) unsigned DEFAULT NULL COMMENT '建议刷新间隔秒数',
  `qr_code_value` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二维码内容、token或地址',
  `seat_info` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '座位信息',
  `seat_zone` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方分配区域',
  `seat_row` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方分配排号',
  `seat_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方分配座号',
  `entrance_info` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '入口/闸机提示',
  `generate_time` datetime DEFAULT NULL COMMENT '电子票码生成成功时间',
  `provider_issue_time` datetime DEFAULT NULL COMMENT '第三方出票时间',
  `last_source_sync_time` datetime DEFAULT NULL COMMENT '最近第三方凭证同步时间',
  `check_time` datetime DEFAULT NULL COMMENT '检票时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `abnormal_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '出票异常、二维码异常原因',
  `refund_hold_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款申请前电子票状态快照，退款驳回时恢复',
  `refund_hold_abnormal_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款申请前异常原因快照，退款驳回时恢复',
  `create_time` datetime NOT NULL COMMENT '电子票记录创建时间',
  `update_time` datetime NOT NULL COMMENT '电子票状态最后更新时间',
  PRIMARY KEY (`ticket_id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  UNIQUE KEY `uk_ticket_order_audience` (`order_audience_id`),
  UNIQUE KEY `uk_ticket_provider_ticket` (`source_provider_id`,`provider_ticket_id`),
  KEY `idx_ticket_order` (`order_id`),
  KEY `idx_ticket_order_item` (`order_item_id`),
  KEY `idx_ticket_status` (`ticket_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电子票';

/*Data for the table `electronic_ticket` */

/*Table structure for table `mock_ticket_source_behavior` */

DROP TABLE IF EXISTS `mock_ticket_source_behavior`;

CREATE TABLE `mock_ticket_source_behavior` (
  `operation_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'HEALTH/QUERY_PROJECTS/GET_PROJECT/QUERY_SESSIONS/QUERY_SKUS/QUERY_INVENTORY',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用延迟或强制错误行为',
  `delay_ms` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '响应延迟毫秒，用于超时测试',
  `forced_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '非空时强制返回模拟第三方错误',
  `forced_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `forced_retryable` tinyint(1) NOT NULL DEFAULT '0',
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`operation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地模拟票源故障与延迟行为';

/*Data for the table `mock_ticket_source_behavior` */

/*Table structure for table `mock_ticket_source_callback_event` */

DROP TABLE IF EXISTS `mock_ticket_source_callback_event`;

CREATE TABLE `mock_ticket_source_callback_event` (
  `event_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_resource_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resource_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `occurred_time` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_mock_v11_callback_resource` (`resource_type`,`provider_resource_id`,`occurred_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方回调事件发件箱';

/*Data for the table `mock_ticket_source_callback_event` */

/*Table structure for table `mock_ticket_source_campaign_asset` */

DROP TABLE IF EXISTS `mock_ticket_source_campaign_asset`;

CREATE TABLE `mock_ticket_source_campaign_asset` (
  `asset_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `asset_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `position_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city_codes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `promotion_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`asset_id`),
  KEY `idx_mock_v11_campaign_time` (`enabled`,`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方活动素材';

/*Data for the table `mock_ticket_source_campaign_asset` */

/*Table structure for table `mock_ticket_source_credential` */

DROP TABLE IF EXISTS `mock_ticket_source_credential`;

CREATE TABLE `mock_ticket_source_credential` (
  `provider_ticket_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ticket_index` int(10) unsigned NOT NULL,
  `client_ticket_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `holder_ref` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ticket_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PENDING/ISSUED/FAILED',
  `credential_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `credential_payload` longtext COLLATE utf8mb4_unicode_ci,
  `credential_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dynamic_qr_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seat_zone` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seat_row` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seat_number` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entrance_info` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issue_time` datetime DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `refresh_after_seconds` int(10) unsigned DEFAULT NULL,
  `validate_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_VALIDATED',
  `error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_ticket_id`),
  UNIQUE KEY `uk_mock_credential_order_index` (`provider_order_id`,`ticket_index`),
  KEY `idx_mock_credential_order_status` (`provider_order_id`,`ticket_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟第三方单张电子凭证';

/*Data for the table `mock_ticket_source_credential` */

/*Table structure for table `mock_ticket_source_delivery` */

DROP TABLE IF EXISTS `mock_ticket_source_delivery`;

CREATE TABLE `mock_ticket_source_delivery` (
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `delivery_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PARTIAL/ISSUED/FAILED',
  `issue_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IMMEDIATE' COMMENT 'IMMEDIATE/DELAYED/PARTIAL_FAIL/ALL_FAIL',
  `issue_trigger_mode` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `seat_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROVIDER_ASSIGNED' COMMENT 'NONE/PROVIDER_ASSIGNED',
  `credential_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'QR_CODE',
  `dynamic_qr_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fail_ticket_index` int(10) unsigned DEFAULT NULL,
  `available_time` datetime DEFAULT NULL,
  `request_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `expected_ticket_count` int(10) unsigned NOT NULL,
  `issued_count` int(10) unsigned NOT NULL DEFAULT '0',
  `failed_count` int(10) unsigned NOT NULL DEFAULT '0',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_order_id`),
  UNIQUE KEY `uk_mock_delivery_idempotency` (`request_idempotency_key`),
  KEY `idx_mock_delivery_status_time` (`delivery_status`,`available_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟第三方出票批次';

/*Data for the table `mock_ticket_source_delivery` */

/*Table structure for table `mock_ticket_source_delivery_quote` */

DROP TABLE IF EXISTS `mock_ticket_source_delivery_quote`;

CREATE TABLE `mock_ticket_source_delivery_quote` (
  `quote_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address_snapshot` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `delivery_fee` decimal(10,2) NOT NULL,
  `expires_time` datetime NOT NULL,
  `used_provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`quote_id`),
  KEY `idx_mock_v11_quote_expire` (`expires_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方快递费报价';

/*Data for the table `mock_ticket_source_delivery_quote` */

/*Table structure for table `mock_ticket_source_order` */

DROP TABLE IF EXISTS `mock_ticket_source_order`;

CREATE TABLE `mock_ticket_source_order` (
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_order_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_model` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SINGLE_SKU',
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int(10) unsigned NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `face_amount` decimal(10,2) DEFAULT NULL,
  `settlement_amount` decimal(10,2) DEFAULT NULL,
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `service_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `pay_amount` decimal(10,2) DEFAULT NULL,
  `ticket_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivery_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `buyer_snapshot` text COLLATE utf8mb4_unicode_ci,
  `contact_snapshot` text COLLATE utf8mb4_unicode_ci,
  `address_snapshot` text COLLATE utf8mb4_unicode_ci,
  `delivery_quote_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issue_trigger_mode` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `order_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'WAIT_PAY/PAID/CANCELED/EXPIRED',
  `create_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cancel_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reservation_expire_time` datetime DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `cancel_time` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_order_id`),
  UNIQUE KEY `uk_mock_order_no` (`provider_order_no`),
  UNIQUE KEY `uk_mock_client_order_no` (`client_order_no`),
  UNIQUE KEY `uk_mock_create_idempotency` (`create_idempotency_key`),
  KEY `idx_mock_order_status_expire` (`order_status`,`reservation_expire_time`),
  KEY `idx_mock_order_sku_time` (`source_sku_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地模拟第三方平台订单';

/*Data for the table `mock_ticket_source_order` */

/*Table structure for table `mock_ticket_source_order_item` */

DROP TABLE IF EXISTS `mock_ticket_source_order_item`;

CREATE TABLE `mock_ticket_source_order_item` (
  `item_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_line_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int(10) unsigned NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `settlement_unit_price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uk_mock_v11_order_line` (`provider_order_id`,`client_line_no`),
  UNIQUE KEY `uk_mock_v11_order_sku` (`provider_order_id`,`source_sku_id`),
  KEY `idx_mock_v11_order_item_order` (`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方订单票档明细';

/*Data for the table `mock_ticket_source_order_item` */

/*Table structure for table `mock_ticket_source_order_ticket` */

DROP TABLE IF EXISTS `mock_ticket_source_order_ticket`;

CREATE TABLE `mock_ticket_source_order_ticket` (
  `ticket_unit_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_ticket_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `holder_ref` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `holder_snapshot` text COLLATE utf8mb4_unicode_ci,
  `provider_sub_order_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_ticket_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ticket_unit_id`),
  UNIQUE KEY `uk_mock_v11_order_ticket_no` (`provider_order_id`,`client_ticket_no`),
  UNIQUE KEY `uk_mock_v11_order_holder` (`provider_order_id`,`holder_ref`),
  UNIQUE KEY `uk_mock_v11_provider_ticket` (`provider_ticket_id`),
  KEY `idx_mock_v11_order_ticket_order` (`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方订单逐票观演人关系';

/*Data for the table `mock_ticket_source_order_ticket` */

/*Table structure for table `mock_ticket_source_project` */

DROP TABLE IF EXISTS `mock_ticket_source_project`;

CREATE TABLE `mock_ticket_source_project` (
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方项目ID',
  `source_project_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方项目名称',
  `project_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SHOW',
  `category_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方分类名称',
  `category_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主要城市',
  `city_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `venue_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主要场馆',
  `venue_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `subtitle` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `introduction` text COLLATE utf8mb4_unicode_ci,
  `detail_html` longtext COLLATE utf8mb4_unicode_ci,
  `show_time_text` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sale_start_time` datetime DEFAULT NULL,
  `sale_end_time` datetime DEFAULT NULL,
  `purchase_limitation_once` int(10) unsigned DEFAULT NULL COMMENT '大麦项目单次限购数量',
  `has_reserved_seat` tinyint(1) NOT NULL DEFAULT '0',
  `poster_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方项目海报地址',
  `detail_content` longtext COLLATE utf8mb4_unicode_ci COMMENT '第三方项目详情快照',
  `sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/PRESALE/ON_SALE/SOLD_OUT/OFF_SHELF/ENDED',
  `source_status_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_status_text` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '最低销售价',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '最高销售价',
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否对模拟接口可见',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`source_project_id`),
  KEY `idx_mock_source_project_query` (`enabled`,`city_name`,`sale_status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地第三方票源模拟项目';

/*Data for the table `mock_ticket_source_project` */

insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-P-390101','「海上回声」2026巡回演唱会上海站','SHOW','演唱会','CONCERT','上海',NULL,'梅赛德斯-奔驰文化中心','DEMO-AI-V-690101',NULL,'跨城市演出示例数据，用于票务功能展示。','<h2>「海上回声」2026巡回演唱会上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>',NULL,NULL,NULL,NULL,0,'yanchanghui_ZhouJieLunYanchu','<h2>「海上回声」2026巡回演唱会上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>','ON_SALE','DEMO_AI_ON_SALE','模拟在售',280.00,1280.00,'DEMO-AI-P-390101-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-P-390102','话剧《梧桐夜话》上海站','SHOW','话剧','DRAMA','上海',NULL,'上海文化广场','DEMO-AI-V-690102',NULL,'跨城市演出示例数据，用于票务功能展示。','<h2>话剧《梧桐夜话》上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>',NULL,NULL,NULL,NULL,0,'huajuYanchu','<h2>话剧《梧桐夜话》上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>','ON_SALE','DEMO_AI_ON_SALE','模拟在售',180.00,680.00,'DEMO-AI-P-390102-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-P-390103','「城市潮汐」2026演唱会上海站','SHOW','演唱会','CONCERT','上海',NULL,'梅赛德斯-奔驰文化中心','DEMO-AI-V-690101',NULL,'跨城市演出示例数据，用于票务功能展示。','<h2>「城市潮汐」2026演唱会上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>',NULL,NULL,NULL,NULL,0,'yanchanghui_ZhouJieLunYanchu','<h2>「城市潮汐」2026演唱会上海站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>','ON_SALE','DEMO_AI_ON_SALE','模拟在售',380.00,1580.00,'DEMO-AI-P-390103-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-P-390104','「珠江夜航」2026巡回演唱会广州站','SHOW','演唱会','CONCERT','广州',NULL,'广州体育馆','DEMO-AI-V-690103',NULL,'跨城市演出示例数据，用于票务功能展示。','<h2>「珠江夜航」2026巡回演唱会广州站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>',NULL,NULL,NULL,NULL,0,'yanchanghui_ZhouJieLunYanchu','<h2>「珠江夜航」2026巡回演唱会广州站</h2><p>跨城市演出示例数据，用于票务功能展示。</p>','ON_SALE','DEMO_AI_ON_SALE','模拟在售',280.00,1280.00,'DEMO-AI-P-390104-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-001','「回声计划」2026巡回演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','凯迪拉克中心','DEMO-MOCK-VENUE-001','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「回声计划」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-20 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「回声计划」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-001-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-002','「银河来信」2026巡回演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','国家大剧院','DEMO-MOCK-VENUE-002','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「银河来信」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-21 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「银河来信」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-002-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-003','「夏日漫游」2026演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','北京展览馆剧场','DEMO-MOCK-VENUE-003','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「夏日漫游」2026演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-22 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「夏日漫游」2026演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-003-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-004','「城市脉搏」2026 Live 巡演北京站','SHOW','演唱会','CONCERT','北京','110100','北京天桥艺术中心','DEMO-MOCK-VENUE-004','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「城市脉搏」2026 Live 巡演北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-23 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「城市脉搏」2026 Live 巡演北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-004-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-005','「无界声场」2026巡回演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','国家体育馆','DEMO-MOCK-VENUE-005','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「无界声场」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-24 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「无界声场」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-005-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-006','「向光而行」2026音乐现场北京站','SHOW','演唱会','CONCERT','北京','110100','北京喜剧院','DEMO-MOCK-VENUE-006','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「向光而行」2026音乐现场北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-25 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「向光而行」2026音乐现场北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-006-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-007','「时差之外」2026巡回演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','凯迪拉克中心','DEMO-MOCK-VENUE-001','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「时差之外」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-26 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「时差之外」2026巡回演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','PRESALE','DEMO_PRESALE','模拟预售',280.00,1280.00,'DEMO-MOCK-PROJ-007-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-PROJ-008','「晚风电台」2026演唱会北京站','SHOW','演唱会','CONCERT','北京','110100','国家大剧院','DEMO-MOCK-VENUE-002','LOCAL_MOCK 演示项目','本地模拟票源项目，用于票务功能展示。','<h2>「晚风电台」2026演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','未来场次','2026-08-15 13:22:32','2026-08-27 19:00:00',4,0,'yanchanghui_ZhouJieLunYanchu','<h2>「晚风电台」2026演唱会北京站</h2><p>本地模拟票源项目，用于票务功能展示。</p>','ON_SALE','DEMO_ON_SALE','模拟在售',280.00,1280.00,'DEMO-MOCK-PROJ-008-v1',1,'2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-PROJ-1001','话剧演出·北京站','SHOW','话剧','DRAMA','北京','110100','国家体育场','MOCK-VENUE-BJ-001','Provider 同步演示简介','第三方模拟票源项目，用于票务功能展示。','<h2>话剧演出·北京站</h2><p>第三方模拟票源项目，用于票务功能展示。</p>','2026年9月20日-21日','2026-08-26 09:00:00','2026-09-21 13:30:00',2,0,'huajuYanchu','<h2>话剧演出·北京站</h2><p>第三方模拟票源项目，用于票务功能展示。</p>','ON_SALE','MOCK_ON_SALE','模拟在售',320.00,1280.00,'DEMO-PROJECT-20260825095630',1,'2026-08-02 23:20:41','2026-08-25 09:56:30');
insert  into `mock_ticket_source_project`(`source_project_id`,`source_project_name`,`project_type`,`category_name`,`category_code`,`city_name`,`city_code`,`venue_name`,`venue_id`,`subtitle`,`introduction`,`detail_html`,`show_time_text`,`sale_start_time`,`sale_end_time`,`purchase_limitation_once`,`has_reserved_seat`,`poster_url`,`detail_content`,`sale_status`,`source_status_code`,`source_status_text`,`min_price`,`max_price`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-PROJ-1003','舞台剧《纸上星河》北京站（纸质票演示）','SHOW','话剧','DRAMA','北京','110100','北京保利剧院（纸质票演示）','MOCK-VENUE-BJ-002','全纸质票项目：支持快递配送与演出场馆现场取票','用于演示纸质票履约方式、快递地址、现场取票以及未知库存。','<h2>纸质票履约演示项目</h2><p>本项目仅用于 LOCAL_MOCK 演示，不代表真实在售演出。</p>','2026年9月5日 19:30','2026-08-01 10:00:00','2026-09-05 18:30:00',4,1,'huajuYanchu','<h2>舞台剧《纸上星河》北京站</h2><p>本项目为纸质票履约模拟项目，支持快递配送与演出场馆现场取票。</p><p>本项目用于模拟大麦分销项目内容详情接口中的 show_detail。</p>','ON_SALE','MOCK_ON_SALE','模拟在售',380.00,680.00,'MOCK-PROJ-1003-demo-v1',1,'2026-08-10 10:47:00','2026-08-23 22:47:25');

/*Table structure for table `mock_ticket_source_project_capability` */

DROP TABLE IF EXISTS `mock_ticket_source_project_capability`;

CREATE TABLE `mock_ticket_source_project_capability` (
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `capability_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `display_text` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`source_project_id`,`capability_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟项目服务能力';

/*Data for the table `mock_ticket_source_project_capability` */

insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-001','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-001','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-001','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-001','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-002','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-002','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-002','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-002','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-003','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-003','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-003','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-003','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-004','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-004','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-004','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-004','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-005','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-005','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-005','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-005','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-006','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-006','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-006','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-006','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-007','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-007','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-007','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-007','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-008','E_TICKET',1,'电子票','DEMO_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-008','NO_REFUND',1,'不可退','MOCK_NO_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-008','ONE_TICKET_ONE_ID',1,'一票一证','DEMO_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('DEMO-MOCK-PROJ-008','REAL_NAME_PURCHASE',1,'实名制购票','DEMO_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','CONDITIONAL_REFUND',1,'条件退','MOCK_CONDITIONAL_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','DYNAMIC_QR',1,'动态二维码','MOCK_DYNAMIC_QR');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','E_TICKET',1,'电子票','MOCK_ETICKET');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','ONE_TICKET_ONE_ID',1,'一票一证','MOCK_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','PAPER_TICKET',1,'纸质票','MOCK_PAPER');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1001','REAL_NAME_PURCHASE',1,'实名制购票','MOCK_REAL_NAME');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1003','CONDITIONAL_REFUND',1,'条件退','MOCK_CONDITIONAL_REFUND');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1003','ONE_TICKET_ONE_ID',1,'一票一证','MOCK_ONE_TICKET_ONE_ID');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1003','PAPER_TICKET',1,'纸质票','MOCK_PAPER');
insert  into `mock_ticket_source_project_capability`(`source_project_id`,`capability_code`,`enabled`,`display_text`,`source_code`) values 
('MOCK-PROJ-1003','REAL_NAME_PURCHASE',1,'实名制购票','MOCK_REAL_NAME');

/*Table structure for table `mock_ticket_source_project_notice` */

DROP TABLE IF EXISTS `mock_ticket_source_project_notice`;

CREATE TABLE `mock_ticket_source_project_notice` (
  `notice_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `notice_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci,
  `priority` int(11) NOT NULL DEFAULT '0',
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`notice_id`),
  UNIQUE KEY `uk_mock_v11_notice` (`source_project_id`,`notice_code`),
  KEY `idx_mock_v11_notice_project` (`source_project_id`,`enabled`,`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方观演须知';

/*Data for the table `mock_ticket_source_project_notice` */

insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(5,'MOCK-PROJ-1001','children_notice','儿童购票说明','儿童一律凭票入场；实名制场次需使用儿童本人有效证件信息绑定观演人。',10,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(6,'MOCK-PROJ-1001','self_get_ticket_notice','取票说明','电子票以票夹中的票码或动态二维码入场；如票档实际出票为纸质票，则以下单后的票夹取票信息为准。',20,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(7,'MOCK-PROJ-1001','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(8,'MOCK-PROJ-1001','deposit_info','寄存说明','现场提供有限寄存区域，具体开放时间和寄存要求以场馆当日公告为准。',40,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(9,'MOCK-PROJ-1001','real_name_notice','实名制说明','本项目实行实名制购票，一张票对应一名观演人，入场人与购票时填写的观演人须一致。',50,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(10,'MOCK-PROJ-1001','prohibited_items','禁止携带物品说明','禁止携带易燃易爆物品、管制器具、专业摄影摄像设备及其他影响现场安全或版权管理的物品。',60,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(11,'MOCK-PROJ-1001','entrance_notice','入场说明','Provider 入场须知发生变化，重新同步后应更新 Provider 来源内容。',70,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(12,'MOCK-PROJ-1001','eticket_notice','电子票说明','电子票生成后请在票夹中查看；静态码或动态码类型以第三方实际出票结果为准。',80,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(13,'MOCK-PROJ-1001','choice_seat_notice','选座说明','当前分销链路不支持用户选座，座位信息以第三方出票结果为准。',90,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(14,'MOCK-PROJ-1001','policy_of_return','退票政策','本项目执行条件退，退款事实由 Provider 同步。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(15,'MOCK-PROJ-1003','children_notice','儿童购票说明','儿童需凭票入场，实名制要求以订单提交页和现场核验规则为准。',10,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(16,'MOCK-PROJ-1003','self_get_ticket_notice','取票说明','纸质票可选择快递配送或演出场馆现场取票；现场取票地点、时间和凭证以下单后的票夹信息为准。',20,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(17,'MOCK-PROJ-1003','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(18,'MOCK-PROJ-1003','deposit_info','寄存说明','场馆寄存能力以现场实际开放情况为准，贵重物品请自行保管。',40,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(19,'MOCK-PROJ-1003','real_name_notice','实名制说明','本项目实行实名制购票，取票和入场时需按现场要求出示与订单观演人一致的有效证件。',50,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(20,'MOCK-PROJ-1003','prohibited_items','禁止携带物品说明','禁止携带易燃易爆物品、管制器具、专业摄影摄像设备及其他影响现场安全或版权管理的物品。',60,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(21,'MOCK-PROJ-1003','entrance_notice','入场说明','纸质票项目请携带有效票据及实名核验所需证件，建议至少提前60分钟到场。',70,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(22,'MOCK-PROJ-1003','eticket_notice','电子票说明','本项目以纸质票履约为主，若部分票档调整为电子票，以第三方实际出票结果为准。',80,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(23,'MOCK-PROJ-1003','choice_seat_notice','选座说明','当前分销链路不支持用户选座，具体座位信息以第三方出票后的纸质票票面为准。',90,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(24,'MOCK-PROJ-1003','policy_of_return','退票政策','本项目支持条件退：距离演出开始7天以上免手续费；距离演出开始7天以内且超过24小时收取10%手续费；距离演出开始24小时内不支持退票。已寄出的纸质票需按第三方要求退回后处理。具体以项目展示页规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(25,'DEMO-MOCK-PROJ-001','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(26,'DEMO-MOCK-PROJ-002','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(27,'DEMO-MOCK-PROJ-003','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(28,'DEMO-MOCK-PROJ-004','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(29,'DEMO-MOCK-PROJ-005','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(30,'DEMO-MOCK-PROJ-006','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(31,'DEMO-MOCK-PROJ-007','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(32,'DEMO-MOCK-PROJ-008','policy_of_return','退票政策','Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。',100,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(88,'DEMO-MOCK-PROJ-001','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(89,'DEMO-MOCK-PROJ-002','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(90,'DEMO-MOCK-PROJ-003','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(91,'DEMO-MOCK-PROJ-004','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(92,'DEMO-MOCK-PROJ-005','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(93,'DEMO-MOCK-PROJ-006','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(94,'DEMO-MOCK-PROJ-007','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);
insert  into `mock_ticket_source_project_notice`(`notice_id`,`source_project_id`,`notice_code`,`title`,`content`,`priority`,`enabled`) values 
(95,'DEMO-MOCK-PROJ-008','limit_notice','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30,1);

/*Table structure for table `mock_ticket_source_promotion_rule` */

DROP TABLE IF EXISTS `mock_ticket_source_promotion_rule`;

CREATE TABLE `mock_ticket_source_promotion_rule` (
  `promotion_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `promotion_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_session_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stackable` tinyint(1) NOT NULL DEFAULT '0',
  `rule_data` text COLLATE utf8mb4_unicode_ci,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime NOT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`promotion_id`),
  KEY `idx_mock_v11_promotion_project` (`source_project_id`,`enabled`,`valid_from`,`valid_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟交易优惠规则';

/*Data for the table `mock_ticket_source_promotion_rule` */

insert  into `mock_ticket_source_promotion_rule`(`promotion_id`,`promotion_type`,`title`,`description`,`source_project_id`,`source_session_id`,`source_sku_id`,`stackable`,`rule_data`,`valid_from`,`valid_to`,`data_version`,`enabled`,`update_time`) values 
('MOCK-PROMO-1001','FULL_REDUCTION','满1000减50','同一订单票款满1000元减50元','MOCK-PROJ-1001',NULL,NULL,0,'{\"thresholdAmountMinor\":100000,\"discountAmountMinor\":5000}','2026-08-01 00:00:00','2026-09-20 13:00:00','mock-promo-1001-v1',0,'2026-08-13 02:06:07');

/*Table structure for table `mock_ticket_source_refund` */

DROP TABLE IF EXISTS `mock_ticket_source_refund`;

CREATE TABLE `mock_ticket_source_refund` (
  `provider_refund_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_refund_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_refund_no` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `refund_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PENDING/PROCESSING/SUCCESS/FAILED',
  `refund_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IMMEDIATE' COMMENT 'IMMEDIATE/DELAYED/REJECT',
  `refund_amount` decimal(10,2) NOT NULL,
  `fee_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `refunded_delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `currency_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `available_time` datetime DEFAULT NULL,
  `refund_time` datetime DEFAULT NULL,
  `error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `inventory_restored` tinyint(1) NOT NULL DEFAULT '0',
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_refund_id`),
  UNIQUE KEY `uk_mock_refund_no` (`provider_refund_no`),
  UNIQUE KEY `uk_mock_refund_client_no` (`client_refund_no`),
  UNIQUE KEY `uk_mock_refund_idempotency` (`request_idempotency_key`),
  KEY `idx_mock_refund_order` (`provider_order_id`),
  KEY `idx_mock_refund_status_time` (`refund_status`,`available_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟第三方退款记录';

/*Data for the table `mock_ticket_source_refund` */

/*Table structure for table `mock_ticket_source_refund_plan` */

DROP TABLE IF EXISTS `mock_ticket_source_refund_plan`;

CREATE TABLE `mock_ticket_source_refund_plan` (
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `refund_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'IMMEDIATE',
  `available_time` datetime DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟第三方退款计划';

/*Data for the table `mock_ticket_source_refund_plan` */

/*Table structure for table `mock_ticket_source_refund_policy` */

DROP TABLE IF EXISTS `mock_ticket_source_refund_policy`;

CREATE TABLE `mock_ticket_source_refund_policy` (
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `refund_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `consumer_entry_enabled` tinyint(1) NOT NULL DEFAULT '0',
  `fee_rule_mode` varchar(48) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivery_fee_refundable` tinyint(1) NOT NULL DEFAULT '0',
  `paper_ticket_return_rule` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_rule_text` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`source_project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟项目退款规则';

/*Data for the table `mock_ticket_source_refund_policy` */

insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-001','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-002','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-003','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-004','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-005','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-006','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-007','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('DEMO-MOCK-PROJ-008','NO_REFUND',0,'NONE',0,NULL,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('MOCK-PROJ-1001','CONDITIONAL_REFUND',1,'RELATIVE_TO_SESSION_START',0,NULL,'距离开演7天以上免费退；7天以内且超过24小时收取15%手续费；24小时内不可退。');
insert  into `mock_ticket_source_refund_policy`(`source_project_id`,`refund_type`,`consumer_entry_enabled`,`fee_rule_mode`,`delivery_fee_refundable`,`paper_ticket_return_rule`,`source_rule_text`) values 
('MOCK-PROJ-1003','CONDITIONAL_REFUND',1,'RELATIVE_TO_SESSION_START',0,'未发货可按规则处理；已寄出的纸质票需按第三方要求退回后再处理。','本项目支持条件退：距离演出开始7天以上免手续费；距离演出开始7天以内且超过24小时收取10%手续费；距离演出开始24小时内不支持退票。已寄出的纸质票需按第三方要求退回后处理。具体以项目展示页规则为准。');

/*Table structure for table `mock_ticket_source_refund_tier` */

DROP TABLE IF EXISTS `mock_ticket_source_refund_tier`;

CREATE TABLE `mock_ticket_source_refund_tier` (
  `tier_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `start_offset_minutes` bigint(20) DEFAULT NULL,
  `end_offset_minutes` bigint(20) DEFAULT NULL,
  `tier_result` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fee_percent` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fee_fixed` decimal(10,2) DEFAULT NULL,
  `sort_no` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`tier_id`),
  KEY `idx_mock_v11_refund_tier` (`source_project_id`,`sort_no`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟退款阶梯';

/*Data for the table `mock_ticket_source_refund_tier` */

insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(9,'MOCK-PROJ-1001',-525600,-10080,'FREE',NULL,NULL,10);
insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(10,'MOCK-PROJ-1001',-10080,-1440,'FEE_PERCENT','15.00',NULL,20);
insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(11,'MOCK-PROJ-1001',-1440,0,'NOT_ALLOWED',NULL,NULL,30);
insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(12,'MOCK-PROJ-1003',-525600,-10080,'FREE',NULL,NULL,10);
insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(13,'MOCK-PROJ-1003',-10080,-1440,'FEE_PERCENT','10.00',NULL,20);
insert  into `mock_ticket_source_refund_tier`(`tier_id`,`source_project_id`,`start_offset_minutes`,`end_offset_minutes`,`tier_result`,`fee_percent`,`fee_fixed`,`sort_no`) values 
(14,'MOCK-PROJ-1003',-1440,0,'NOT_ALLOWED',NULL,NULL,30);

/*Table structure for table `mock_ticket_source_session` */

DROP TABLE IF EXISTS `mock_ticket_source_session`;

CREATE TABLE `mock_ticket_source_session` (
  `source_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方场次ID',
  `source_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方项目ID',
  `source_session_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方场次名称',
  `city_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `venue_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `venue_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方场馆地址',
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `sale_start_time` datetime DEFAULT NULL,
  `sale_end_time` datetime DEFAULT NULL,
  `sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN',
  `source_status_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_status_text` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `session_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SINGLE',
  `seat_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL_ADMISSION',
  `time_changed` tinyint(1) NOT NULL DEFAULT '0',
  `change_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `limit_per_order` int(10) unsigned NOT NULL DEFAULT '2',
  `real_name_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issue_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pickup_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`source_session_id`),
  KEY `idx_mock_source_session_project` (`source_project_id`,`enabled`,`start_time`),
  KEY `idx_mock_source_session_sale` (`enabled`,`sale_status`,`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地第三方票源模拟场次';

/*Data for the table `mock_ticket_source_session` */

insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-S-490101','DEMO-AI-P-390101','上海站·9月2日场','上海','梅赛德斯-奔驰文化中心','上海市浦东新区世博大道1200号','2026-09-02 19:30:00','2026-09-02 21:30:00',NULL,NULL,'ON_SALE','DEMO_AI_ON_SALE','模拟在售','SINGLE','GENERAL_ADMISSION',0,NULL,NULL,4,NULL,NULL,NULL,'DEMO-AI-S-490101-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-S-490102','DEMO-AI-P-390102','上海站·9月5日场','上海','上海文化广场','上海市黄浦区复兴中路597号','2026-09-05 19:30:00','2026-09-05 21:30:00',NULL,NULL,'ON_SALE','DEMO_AI_ON_SALE','模拟在售','SINGLE','GENERAL_ADMISSION',0,NULL,NULL,4,NULL,NULL,NULL,'DEMO-AI-S-490102-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-S-490103','DEMO-AI-P-390103','上海站·9月18日场','上海','梅赛德斯-奔驰文化中心','上海市浦东新区世博大道1200号','2026-09-18 19:30:00','2026-09-18 21:30:00',NULL,NULL,'ON_SALE','DEMO_AI_ON_SALE','模拟在售','SINGLE','GENERAL_ADMISSION',0,NULL,NULL,4,NULL,NULL,NULL,'DEMO-AI-S-490103-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-AI-S-490104','DEMO-AI-P-390104','广州站·9月3日场','广州','广州体育馆','广州市白云区白云大道南783号','2026-09-03 19:30:00','2026-09-03 21:30:00',NULL,NULL,'ON_SALE','DEMO_AI_ON_SALE','模拟在售','SINGLE','GENERAL_ADMISSION',0,NULL,NULL,4,NULL,NULL,NULL,'DEMO-AI-S-490104-v1',1,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-001','DEMO-MOCK-PROJ-001','北京站 · 08月20日 19:30','北京','凯迪拉克中心','北京市海淀区复兴路69号','2026-08-20 19:30:00','2026-08-20 21:30:00','2026-08-15 13:22:32','2026-08-20 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-001-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-002','DEMO-MOCK-PROJ-002','北京站 · 08月21日 19:30','北京','国家大剧院','北京市西城区西长安街2号','2026-08-21 19:30:00','2026-08-21 21:30:00','2026-08-15 13:22:32','2026-08-21 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-002-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-003','DEMO-MOCK-PROJ-003','北京站 · 08月22日 19:30','北京','北京展览馆剧场','北京市西城区西直门外大街135号','2026-08-22 19:30:00','2026-08-22 21:30:00','2026-08-15 13:22:32','2026-08-22 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-003-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-004','DEMO-MOCK-PROJ-004','北京站 · 08月23日 19:30','北京','北京天桥艺术中心','北京市西城区天桥南大街9号','2026-08-23 19:30:00','2026-08-23 21:30:00','2026-08-15 13:22:32','2026-08-23 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-004-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-005','DEMO-MOCK-PROJ-005','北京站 · 08月24日 19:30','北京','国家体育馆','北京市朝阳区天辰东路9号','2026-08-24 19:30:00','2026-08-24 21:30:00','2026-08-15 13:22:32','2026-08-24 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-005-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-006','DEMO-MOCK-PROJ-006','北京站 · 08月25日 19:30','北京','北京喜剧院','北京市东城区朝阳门北大街11号','2026-08-25 19:30:00','2026-08-25 21:30:00','2026-08-15 13:22:32','2026-08-25 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-006-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-007','DEMO-MOCK-PROJ-007','北京站 · 08月26日 19:30','北京','凯迪拉克中心','北京市海淀区复兴路69号','2026-08-26 19:30:00','2026-08-26 21:30:00','2026-08-15 13:22:32','2026-08-26 19:00:00','PRESALE','DEMO_PRESALE','模拟预售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-007-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-SESSION-008','DEMO-MOCK-PROJ-008','北京站 · 08月27日 19:30','北京','国家大剧院','北京市西城区西长安街2号','2026-08-27 19:30:00','2026-08-27 21:30:00','2026-08-15 13:22:32','2026-08-27 19:00:00','ON_SALE','DEMO_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,4,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET','DEMO-MOCK-SESSION-008-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-SESSION-11001','MOCK-PROJ-1001','北京站 · 09月12日 19:30','北京','国家体育场','北京市朝阳区 Provider 演示场馆地址','2026-09-12 19:30:00','2026-09-12 22:30:00','2026-08-26 10:00:00','2026-09-12 19:00:00','ON_SALE','MOCK_ON_SALE','模拟在售','SINGLE','AREA_ONLY',0,NULL,NULL,2,'ONE_TICKET_ONE_ID','PROVIDER_FULFILLMENT','E_TICKET_OR_EXPRESS','DEMO-SESSION-20260825095630',1,'2026-08-02 23:20:41','2026-08-25 09:56:30');
insert  into `mock_ticket_source_session`(`source_session_id`,`source_project_id`,`source_session_name`,`city_name`,`venue_name`,`venue_address`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`sale_status`,`source_status_code`,`source_status_text`,`session_type`,`seat_mode`,`time_changed`,`change_reason`,`remark`,`limit_per_order`,`real_name_mode`,`issue_method`,`pickup_method`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-SESSION-12001','MOCK-PROJ-1003','北京站·9月5日 19:30','北京','北京保利剧院（纸质票演示）','北京市东城区东直门南大街14号','2026-09-05 19:30:00','2026-09-05 22:00:00','2026-08-01 10:00:00','2026-09-05 18:30:00','ON_SALE','MOCK_ON_SALE','模拟在售','SINGLE','ASSIGNED_SEAT',0,NULL,'系统分配座位，不支持用户选座',4,'ONE_TICKET_ONE_ID','PAPER_TICKET','EXPRESS_OR_SELF_PICKUP','MOCK-SESSION-12001-demo-v1',1,'2026-08-10 10:47:00','2026-08-10 10:47:00');

/*Table structure for table `mock_ticket_source_shipment` */

DROP TABLE IF EXISTS `mock_ticket_source_shipment`;

CREATE TABLE `mock_ticket_source_shipment` (
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `shipment_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAIT_SHIPMENT',
  `carrier_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `carrier_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `waybill_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipped_time` datetime DEFAULT NULL,
  `signed_time` datetime DEFAULT NULL,
  `tracking_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`provider_order_id`),
  KEY `idx_mock_v11_shipment_status` (`shipment_status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方物流单';

/*Data for the table `mock_ticket_source_shipment` */

/*Table structure for table `mock_ticket_source_sku` */

DROP TABLE IF EXISTS `mock_ticket_source_sku`;

CREATE TABLE `mock_ticket_source_sku` (
  `source_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方票档ID',
  `source_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟第三方场次ID',
  `source_sku_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SINGLE',
  `face_price` decimal(10,2) DEFAULT NULL,
  `sale_price` decimal(10,2) NOT NULL,
  `settlement_price` decimal(10,2) DEFAULT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `inventory_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SNAPSHOT' COMMENT 'UNKNOWN/SNAPSHOT/REALTIME_QUERY',
  `available_stock` int(10) unsigned DEFAULT NULL COMMENT 'NULL表示外部票源未返回库存，不等于0',
  `max_quantity_per_order` int(10) unsigned DEFAULT NULL,
  `sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN',
  `source_status_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `source_status_text` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sub_status` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `version` int(10) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`source_sku_id`),
  KEY `idx_mock_source_sku_session` (`source_session_id`,`enabled`,`sale_price`),
  KEY `idx_mock_source_sku_inventory` (`enabled`,`sale_status`,`available_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地第三方票源模拟票档与库存';

/*Data for the table `mock_ticket_source_sku` */

insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590201','DEMO-AI-S-490101','普通票 280','SINGLE',280.00,280.00,280.00,'CNY','SNAPSHOT',60,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590201-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590202','DEMO-AI-S-490101','VIP票 1280','SINGLE',1280.00,1280.00,1280.00,'CNY','SNAPSHOT',20,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590202-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590203','DEMO-AI-S-490102','普通票 180','SINGLE',180.00,180.00,180.00,'CNY','SNAPSHOT',70,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590203-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590204','DEMO-AI-S-490102','优享票 680','SINGLE',680.00,680.00,680.00,'CNY','SNAPSHOT',25,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590204-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590205','DEMO-AI-S-490103','普通票 380','SINGLE',380.00,380.00,380.00,'CNY','SNAPSHOT',50,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590205-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590206','DEMO-AI-S-490103','VIP票 1580','SINGLE',1580.00,1580.00,1580.00,'CNY','SNAPSHOT',18,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590206-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590207','DEMO-AI-S-490104','普通票 280','SINGLE',280.00,280.00,280.00,'CNY','SNAPSHOT',65,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590207-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-AI-K-590208','DEMO-AI-S-490104','VIP票 1280','SINGLE',1280.00,1280.00,1280.00,'CNY','SNAPSHOT',21,4,'ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源',NULL,'DEMO-AI-K-590208-v1',1,0,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-001-A','DEMO-MOCK-SESSION-001','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',49,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-001-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-001-B','DEMO-MOCK-SESSION-001','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',19,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-001-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-002-A','DEMO-MOCK-SESSION-002','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',50,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-002-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-002-B','DEMO-MOCK-SESSION-002','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',20,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-002-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-003-A','DEMO-MOCK-SESSION-003','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',51,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-003-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-003-B','DEMO-MOCK-SESSION-003','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',21,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-003-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-004-A','DEMO-MOCK-SESSION-004','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',52,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-004-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-004-B','DEMO-MOCK-SESSION-004','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',22,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-004-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-005-A','DEMO-MOCK-SESSION-005','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',53,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-005-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-005-B','DEMO-MOCK-SESSION-005','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',23,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-005-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-006-A','DEMO-MOCK-SESSION-006','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',54,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-006-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-006-B','DEMO-MOCK-SESSION-006','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',24,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-006-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-007-A','DEMO-MOCK-SESSION-007','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',55,4,'PRESALE','DEMO_PRESALE','模拟预售',NULL,'DEMO-MOCK-SKU-007-A-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-007-B','DEMO-MOCK-SESSION-007','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',18,4,'PRESALE','DEMO_PRESALE','模拟预售',NULL,'DEMO-MOCK-SKU-007-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-008-A','DEMO-MOCK-SESSION-008','普通票 280','SINGLE',280.00,280.00,257.60,'CNY','REALTIME_QUERY',54,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-008-A-v2',1,2,'2026-08-17 13:22:32','2026-08-26 05:23:09');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('DEMO-MOCK-SKU-008-B','DEMO-MOCK-SESSION-008','优享票 1280','SINGLE',1280.00,1280.00,1177.60,'CNY','REALTIME_QUERY',19,4,'ON_SALE','DEMO_ON_SALE','模拟在售',NULL,'DEMO-MOCK-SKU-008-B-v1',1,0,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('MOCK-SKU-21001','MOCK-SESSION-11001','票档 ¥366','SINGLE',380.00,366.00,311.00,'CNY','REALTIME_QUERY',65,2,'ON_SALE','ON_SALE','模拟在售',NULL,'DEMO-SKU-20260825095630',1,200,'2026-08-02 23:20:41','2026-08-25 09:56:30');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('MOCK-SKU-21002','MOCK-SESSION-11001','单日 VIP 880','SINGLE',880.00,880.00,820.00,'CNY','REALTIME_QUERY',32,4,'ON_SALE','MOCK_ON_SALE','模拟在售',NULL,'MOCK-SKU-21002-demo-v1',1,26,'2026-08-02 23:20:41','2026-08-14 23:13:55');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('MOCK-SKU-21007','MOCK-SESSION-11001','纪念纸质票 1280','SINGLE',1280.00,1280.00,1180.00,'CNY','REALTIME_QUERY',22,2,'ON_SALE','MOCK_ON_SALE','模拟在售','EXPRESS_SUPPORTED','MOCK-SKU-21007-demo-v1',1,31,'2026-08-06 17:25:57','2026-08-12 22:51:37');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('MOCK-SKU-22001','MOCK-SESSION-12001','一楼普通席 380（纸质票）','SINGLE',380.00,380.00,350.00,'CNY','REALTIME_QUERY',26,4,'ON_SALE','MOCK_ON_SALE','模拟在售','PAPER_TICKET,EXPRESS_SUPPORTED,SELF_PICKUP_SUPPORTED','MOCK-SKU-22001-demo-v1',1,19,'2026-08-10 10:47:00','2026-09-03 00:37:01');
insert  into `mock_ticket_source_sku`(`source_sku_id`,`source_session_id`,`source_sku_name`,`product_type`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`inventory_mode`,`available_stock`,`max_quantity_per_order`,`sale_status`,`source_status_code`,`source_status_text`,`sub_status`,`data_version`,`enabled`,`version`,`create_time`,`update_time`) values 
('MOCK-SKU-22002','MOCK-SESSION-12001','一楼前排席 680（纸质票·未知库存）','SINGLE',680.00,680.00,640.00,'CNY','SNAPSHOT',NULL,2,'ON_SALE','MOCK_ON_SALE','模拟在售·库存待实时确认','PAPER_TICKET,EXPRESS_SUPPORTED,SELF_PICKUP_SUPPORTED','MOCK-SKU-22002-demo-v1',1,3,'2026-08-10 10:47:00','2026-09-03 00:09:39');

/*Table structure for table `mock_ticket_source_venue` */

DROP TABLE IF EXISTS `mock_ticket_source_venue`;

CREATE TABLE `mock_ticket_source_venue` (
  `venue_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `venue_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `country_code` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'CN',
  `province_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `district_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `longitude` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `latitude` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `coordinate_system` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GCJ02',
  `navigation_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_version` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`venue_id`),
  KEY `idx_mock_v11_venue_city` (`city_code`,`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1模拟第三方场馆';

/*Data for the table `mock_ticket_source_venue` */

insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-001','凯迪拉克中心','CN','110000','110100',NULL,'北京市海淀区复兴路69号','116.2741000','39.9108000','GCJ02','凯迪拉克中心','DEMO-MOCK-VENUE-001-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-002','国家大剧院','CN','110000','110100',NULL,'北京市西城区西长安街2号','116.3830000','39.9033000','GCJ02','国家大剧院','DEMO-MOCK-VENUE-002-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-003','北京展览馆剧场','CN','110000','110100',NULL,'北京市西城区西直门外大街135号','116.3440000','39.9400000','GCJ02','北京展览馆剧场','DEMO-MOCK-VENUE-003-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-004','北京天桥艺术中心','CN','110000','110100',NULL,'北京市西城区天桥南大街9号','116.3980000','39.8830000','GCJ02','北京天桥艺术中心','DEMO-MOCK-VENUE-004-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-005','国家体育馆','CN','110000','110100',NULL,'北京市朝阳区天辰东路9号','116.3870000','39.9990000','GCJ02','国家体育馆','DEMO-MOCK-VENUE-005-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('DEMO-MOCK-VENUE-006','北京喜剧院','CN','110000','110100',NULL,'北京市东城区朝阳门北大街11号','116.4300000','39.9280000','GCJ02','北京喜剧院','DEMO-MOCK-VENUE-006-demo-v1',1,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-VENUE-BJ-001','国家体育场','CN','110000','110100','110105','北京市朝阳区 Provider 演示场馆地址','116.397477','39.992865','GCJ02','国家体育场','DEMO-VENUE-20260825095630',1,'2026-08-06 17:25:57','2026-08-25 09:56:30');
insert  into `mock_ticket_source_venue`(`venue_id`,`venue_name`,`country_code`,`province_code`,`city_code`,`district_code`,`address`,`longitude`,`latitude`,`coordinate_system`,`navigation_name`,`data_version`,`enabled`,`create_time`,`update_time`) values 
('MOCK-VENUE-BJ-002','北京保利剧院','CN','110000','110100','110101','北京市东城区东直门南大街14号','116.438100','39.933500','GCJ02','北京保利剧院','MOCK-VENUE-BJ-002-demo-v1',1,'2026-08-10 10:47:00','2026-08-10 10:47:00');

/*Table structure for table `notice_item` */

DROP TABLE IF EXISTS `notice_item`;

CREATE TABLE `notice_item` (
  `notice_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '须知主键',
  `title` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '须知标题',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '须知内容',
  `icon_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图标资源名或地址',
  `notice_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '须知类型，可选',
  PRIMARY KEY (`notice_id`),
  UNIQUE KEY `uk_notice_title` (`title`),
  KEY `idx_notice_type` (`notice_type`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='观演须知字典';

/*Data for the table `notice_item` */

insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(1,'限购规则','具体限购数量以当前项目购票规则和提交订单页展示为准。','item_xiangouguize','LIMIT');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(2,'入场方式','电子票生成后，请在票夹中出示二维码，现场核验通过后入场；纸质票项目请携带有效票据入场。','item_ruchangfangshi','ENTRY');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(3,'儿童购票','儿童是否需要购票以项目现场规则为准。实名制项目中，儿童如需入场也应按要求填写对应观演人信息。','item_ertonggoupiao','CHILD');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(4,'发票说明','本期暂不支持在线申请电子发票。后续可在订单详情页补充发票申请入口。','item_fapiaoshuoming','INVOICE');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(5,'实名购票','本项目为实名制购票，一张票对应一名观演人。下单时需选择与购票数量一致的观演人。','item_shimingzhi','REAL_NAME');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(6,'退票规则','退票规则以演出项目配置为准。条件退项目会根据距离开演时间计算手续费；不可退项目支付成功后不支持主动退款。','item_tuipiaoguize','REFUND');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(7,'禁带物品','请勿携带易燃易爆物品、管制器具、瓶装饮料、大型行李、专业摄影摄像设备等影响现场安全或观演秩序的物品。','item_jindaiwupin','SECURITY');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(8,'安检说明','入场前需配合现场工作人员进行票务核验、证件核验和安全检查。请预留充足入场时间。','item_anjian','SECURITY');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(11,'注意踩踏','演唱会观演人过多，请注意安全','item_anjian','SECURITY');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(12,'电子票说明','静态码、动态码以第三方实际出票类型为准。','item_anjian','E_TICKET_NOTICE');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(13,'选座说明','不支持用户选座，出票后展示第三方分配的座位。','item_anjian','SEAT_SELECTION_NOTICE');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(14,'纸质票说明','快递票寄送至订单收货地址；现场取票须于演出当天提前到演出场馆领取。','item_anjian','PAPER_TICKET_NOTICE');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(15,'寄存说明','现场提供有限寄存区域，具体开放时间和寄存要求以场馆当日公告为准。','item_anjian','STORAGE');
insert  into `notice_item`(`notice_id`,`title`,`description`,`icon_url`,`notice_type`) values 
(16,'异常排单说明','为了确保广大消费者的利益，对于异常订单行为，麦麦演出有权取消相应订单并且通过系统原来退回该订单','/media/notice/icon/2026/08/20260824101430_4facb1e3_notice_yichangpaidan.svg',NULL);

/*Table structure for table `order_address` */

DROP TABLE IF EXISTS `order_address`;

CREATE TABLE `order_address` (
  `order_address_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单地址快照主键',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '所属订单',
  `receiver_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名快照',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号快照',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省快照',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市快照',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县快照',
  `country_code` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家/地区编码快照',
  `province_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省级行政区编码快照',
  `city_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市行政区编码快照',
  `area_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县行政区编码快照',
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址快照',
  PRIMARY KEY (`order_address_id`),
  UNIQUE KEY `uk_order_address_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单收货地址快照';

/*Data for the table `order_address` */

/*Table structure for table `order_audience` */

DROP TABLE IF EXISTS `order_audience`;

CREATE TABLE `order_audience` (
  `order_audience_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单观演人快照主键',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '所属订单',
  `order_item_id` bigint(20) unsigned DEFAULT NULL COMMENT '该观演人绑定的订单票档',
  `client_ticket_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本地逐票请求号',
  `holder_ref` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'V1.2第三方逐票观演人引用',
  `real_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '观演人姓名快照',
  `certificate_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件类型快照',
  `certificate_no` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号快照，加密存储预留',
  `certificate_no_hash` char(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证件号哈希，用于同场次实名查重',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号快照，可空',
  PRIMARY KEY (`order_audience_id`),
  UNIQUE KEY `uk_order_audience_client_ticket` (`order_id`,`client_ticket_no`),
  UNIQUE KEY `uk_order_audience_holder_ref` (`order_id`,`holder_ref`),
  KEY `idx_order_audience_order` (`order_id`),
  KEY `idx_order_audience_cert_hash` (`certificate_no_hash`),
  KEY `idx_order_audience_item` (`order_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单观演人快照';

/*Data for the table `order_audience` */

/*Table structure for table `order_item` */

DROP TABLE IF EXISTS `order_item`;

CREATE TABLE `order_item` (
  `order_item_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单项主键',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '所属订单ID',
  `sku_id` bigint(20) unsigned NOT NULL COMMENT '下单时选择的票档ID',
  `sku_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单时票档名称快照',
  `unit_price` decimal(10,2) NOT NULL COMMENT '下单时单价快照',
  `face_price` decimal(10,2) DEFAULT NULL COMMENT '第三方票面单价',
  `provider_sale_price` decimal(10,2) DEFAULT NULL COMMENT '第三方渠道销售单价',
  `settlement_price` decimal(10,2) DEFAULT NULL COMMENT '与第三方结算单价',
  `quantity` int(10) unsigned NOT NULL COMMENT '购买数量',
  `subtotal_amount` decimal(10,2) NOT NULL COMMENT '票款小计',
  PRIMARY KEY (`order_item_id`),
  UNIQUE KEY `uk_order_item_order_sku` (`order_id`,`sku_id`),
  KEY `idx_order_item_sku` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项';

/*Data for the table `order_item` */

/*Table structure for table `performance_project` */

DROP TABLE IF EXISTS `performance_project`;

CREATE TABLE `performance_project` (
  `project_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '演出项目主键',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '演出项目名称',
  `category_id` bigint(20) unsigned NOT NULL COMMENT '分类ID',
  `poster_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主海报',
  `detail_content` longtext COLLATE utf8mb4_unicode_ci COMMENT '项目默认详情',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '项目最低价缓存',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '项目最高价缓存',
  `want_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '想看人数缓存',
  `hot_score` decimal(10,2) DEFAULT NULL COMMENT '热度/综合排序分',
  `project_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目整体状态',
  `recommend_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否首页推荐',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`project_id`),
  KEY `idx_project_category` (`category_id`),
  KEY `idx_project_status` (`project_status`),
  KEY `idx_project_recommend` (`recommend_flag`,`project_status`,`hot_score`),
  KEY `idx_project_publish_time` (`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=390109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出项目';

/*Data for the table `performance_project` */

insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(1015,'话剧演出·北京站',2,'huajuYanchu','<h2>话剧演出·北京站</h2><p>本项目由第三方票源提供演出和票务信息，具体场次、票档和服务规则以页面展示为准。</p>',366.00,1280.00,0,555.55,'COMING_SOON',0,'2026-08-20 08:08:08','2026-08-03 00:47:28','2026-09-02 15:55:06');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(190003,'舞台剧《纸上星河》北京站',2,'huajuYanchu','<h2>舞台剧《纸上星河》北京站</h2><p>本项目支持纸质票，可选择快递配送或演出场馆现场取票。</p><p>具体出票及配送信息以下单页面展示为准。</p>',380.00,680.00,0,0.00,'ON_SALE',1,'2026-08-10 10:50:11','2026-08-10 10:50:11','2026-08-14 23:58:15');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300001,'「回声计划」2026巡回演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「回声计划」2026巡回演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,98.50,'ENDED',0,'2026-08-17 12:22:32','2026-08-17 13:22:32','2026-08-21 08:51:22');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300002,'「银河来信」2026巡回演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「银河来信」2026巡回演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,97.35,'ENDED',0,'2026-08-17 11:22:32','2026-08-17 13:22:32','2026-08-27 00:01:42');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300003,'「夏日漫游」2026演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「夏日漫游」2026演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,96.20,'ENDED',0,'2026-08-17 10:22:32','2026-08-17 13:22:32','2026-08-27 00:01:48');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300004,'「城市脉搏」2026 Live 巡演北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「城市脉搏」2026 Live 巡演北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,95.05,'ENDED',0,'2026-08-17 09:22:32','2026-08-17 13:22:32','2026-08-27 00:01:55');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300005,'「无界声场」2026巡回演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「无界声场」2026巡回演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,93.90,'ON_SALE',0,'2026-08-17 08:22:32','2026-08-17 13:22:32','2026-08-28 01:15:29');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300006,'「向光而行」2026音乐现场北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「向光而行」2026音乐现场北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,92.75,'ON_SALE',1,'2026-08-17 07:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300007,'「时差之外」2026巡回演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「时差之外」2026巡回演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,91.60,'COMING_SOON',1,'2026-08-17 06:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(300008,'「晚风电台」2026演唱会北京站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「晚风电台」2026演唱会北京站</h2><p>本项目为本地模拟票源演出数据，用于票务浏览、演出详情与购票流程展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,90.45,'ON_SALE',1,'2026-08-17 05:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(390101,'「海上回声」2026巡回演唱会上海站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「海上回声」2026巡回演唱会上海站</h2><p>跨城市演出示例数据，用于演出浏览、搜索与票务功能展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,92.10,'ON_SALE',1,'2026-08-28 10:00:00','2026-08-30 17:28:13','2026-08-31 11:54:19');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(390102,'话剧《梧桐夜话》上海站',2,'huajuYanchu','<h2>话剧《梧桐夜话》上海站</h2><p>跨城市演出示例数据，用于演出浏览、搜索与票务功能展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',180.00,680.00,0,86.20,'ON_SALE',1,'2026-08-28 10:10:00','2026-08-30 17:28:13','2026-08-31 11:54:19');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(390103,'「城市潮汐」2026演唱会上海站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「城市潮汐」2026演唱会上海站</h2><p>跨城市演出示例数据，用于演出浏览、搜索与票务功能展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',380.00,1580.00,0,90.50,'ON_SALE',1,'2026-08-28 10:20:00','2026-08-30 17:28:13','2026-08-31 11:54:19');
insert  into `performance_project`(`project_id`,`title`,`category_id`,`poster_url`,`detail_content`,`min_price`,`max_price`,`want_count`,`hot_score`,`project_status`,`recommend_flag`,`publish_time`,`create_time`,`update_time`) values 
(390104,'「珠江夜航」2026巡回演唱会广州站',1,'yanchanghui_ZhouJieLunYanchu','<h2>「珠江夜航」2026巡回演唱会广州站</h2><p>跨城市演出示例数据，用于演出浏览、搜索与票务功能展示。</p><p>实际票务信息以票源平台同步结果为准。</p>',280.00,1280.00,0,89.80,'ON_SALE',1,'2026-08-28 10:30:00','2026-08-30 17:28:13','2026-08-31 11:54:19');

/*Table structure for table `performance_session` */

DROP TABLE IF EXISTS `performance_session`;

CREATE TABLE `performance_session` (
  `session_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '场次主键',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '所属项目ID',
  `city_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市名',
  `station_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '站点名',
  `venue_id` bigint(20) unsigned NOT NULL COMMENT '场馆ID',
  `start_time` datetime NOT NULL COMMENT '演出开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '演出结束时间',
  `sale_start_time` datetime DEFAULT NULL COMMENT '开售时间',
  `sale_end_time` datetime DEFAULT NULL COMMENT '停售时间',
  `issue_offset_hours` int(10) unsigned DEFAULT NULL COMMENT '出票提前小时数',
  `session_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场次状态',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '当前场次最低价缓存',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '当前场次最高价缓存',
  `limit_per_order` int(10) unsigned NOT NULL COMMENT '每单限购',
  `station_detail_content` longtext COLLATE utf8mb4_unicode_ci COMMENT '当前站点专属详情',
  `delivery_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送方式',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`session_id`),
  KEY `idx_session_project` (`project_id`),
  KEY `idx_session_city_status_start` (`city_name`,`session_status`,`start_time`),
  KEY `idx_session_project_city_status` (`project_id`,`city_name`,`session_status`),
  KEY `idx_session_venue` (`venue_id`)
) ENGINE=InnoDB AUTO_INCREMENT=490109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出场次/城市站点';

/*Data for the table `performance_session` */

insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(12015,1015,'北京','北京站 · 09月12日 19:30',9,'2026-09-12 19:30:00','2026-09-12 22:30:00','2026-08-26 10:00:00','2026-09-12 19:00:00',NULL,'ON_SALE',366.00,1280.00,2,NULL,'MIXED','2026-08-03 00:47:29','2026-08-25 09:58:03');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(190003,190003,'北京','北京站·9月5日 19:30',11,'2026-09-05 19:30:00','2026-09-05 22:00:00','2026-08-01 10:00:00','2026-09-05 18:30:00',NULL,'ON_SALE',380.00,680.00,4,NULL,'PAPER_TICKET','2026-08-10 10:50:11','2026-08-23 22:47:25');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400001,300001,'北京','北京站',600001,'2026-08-20 19:30:00','2026-08-20 21:30:00','2026-08-15 13:22:32','2026-08-20 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400002,300002,'北京','北京站',600002,'2026-08-21 19:30:00','2026-08-21 21:30:00','2026-08-15 13:22:32','2026-08-21 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400003,300003,'北京','北京站',600003,'2026-08-22 19:30:00','2026-08-22 21:30:00','2026-08-15 13:22:32','2026-08-22 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400004,300004,'北京','北京站',600004,'2026-08-23 19:30:00','2026-08-23 21:30:00','2026-08-15 13:22:32','2026-08-23 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-23 22:47:25');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400005,300005,'北京','北京站',600005,'2026-08-24 19:30:00','2026-08-24 21:30:00','2026-08-15 13:22:32','2026-08-24 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-24 22:44:26');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400006,300006,'北京','北京站',600006,'2026-08-25 19:30:00','2026-08-25 21:30:00','2026-08-15 13:22:32','2026-08-25 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-25 22:30:12');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400007,300007,'北京','北京站',600001,'2026-08-26 19:30:00','2026-08-26 21:30:00','2026-08-15 13:22:32','2026-08-26 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-26 23:16:22');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(400008,300008,'北京','北京站',600002,'2026-08-27 19:30:00','2026-08-27 21:30:00','2026-08-15 13:22:32','2026-08-27 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-17 13:22:32','2026-08-28 00:58:05');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(490101,390101,'上海','上海站·9月2日场',690101,'2026-09-02 19:30:00','2026-09-02 21:30:00','2026-08-20 10:00:00','2026-09-02 19:00:00',24,'ENDED',280.00,1280.00,4,NULL,'ETICKET','2026-08-30 17:28:13','2026-09-02 19:40:14');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(490102,390102,'上海','上海站·9月5日场',690102,'2026-09-05 19:30:00','2026-09-05 21:30:00','2026-08-20 10:00:00','2026-09-05 19:00:00',24,'ON_SALE',180.00,680.00,4,NULL,'ETICKET','2026-08-30 17:28:13','2026-08-31 11:54:19');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(490103,390103,'上海','上海站·9月18日场',690101,'2026-09-18 19:30:00','2026-09-18 21:30:00','2026-08-25 10:00:00','2026-09-18 19:00:00',24,'ON_SALE',380.00,1580.00,4,NULL,'ETICKET','2026-08-30 17:28:13','2026-08-31 11:54:19');
insert  into `performance_session`(`session_id`,`project_id`,`city_name`,`station_name`,`venue_id`,`start_time`,`end_time`,`sale_start_time`,`sale_end_time`,`issue_offset_hours`,`session_status`,`min_price`,`max_price`,`limit_per_order`,`station_detail_content`,`delivery_type`,`create_time`,`update_time`) values 
(490104,390104,'广州','广州站·9月3日场',690103,'2026-09-03 19:30:00','2026-09-03 21:30:00','2026-08-20 10:00:00','2026-09-03 19:00:00',24,'ON_SALE',280.00,1280.00,4,NULL,'ETICKET','2026-08-30 17:28:13','2026-08-31 11:54:19');

/*Table structure for table `project_notice_rel` */

DROP TABLE IF EXISTS `project_notice_rel`;

CREATE TABLE `project_notice_rel` (
  `rel_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '关联主键',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '项目ID',
  `notice_id` bigint(20) unsigned NOT NULL COMMENT '须知ID',
  `source_provider_id` bigint(20) unsigned DEFAULT NULL COMMENT '第三方来源；空表示本地运营',
  `provider_notice_key` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方须知稳定键',
  `title_override` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方项目级标题覆盖',
  `description_override` text COLLATE utf8mb4_unicode_ci COMMENT '第三方项目级内容覆盖',
  `sort_order` int(10) unsigned NOT NULL COMMENT '项目下展示顺序',
  PRIMARY KEY (`rel_id`),
  UNIQUE KEY `uk_project_notice` (`project_id`,`notice_id`),
  KEY `idx_project_notice_sort` (`project_id`,`sort_order`),
  KEY `idx_project_notice_notice` (`notice_id`),
  KEY `idx_project_notice_source` (`project_id`,`source_provider_id`,`provider_notice_key`)
) ENGINE=InnoDB AUTO_INCREMENT=478 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目特殊观演须知关联';

/*Data for the table `project_notice_rel` */

insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(329,1015,3,NULL,NULL,NULL,NULL,1);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(330,1015,4,NULL,NULL,NULL,NULL,2);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(469,1015,14,1,'PICKUP_NOTICE','取票说明','电子票以票夹中的票码或动态二维码入场；如票档实际出票为纸质票，则以下单后的票夹取票信息为准。',20);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(470,1015,1,1,'PURCHASE_LIMIT','限购说明','立即购买每单最多4张，具体以提交订单页展示为准。',30);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(471,1015,15,1,'STORAGE_NOTICE','寄存说明','现场提供有限寄存区域，具体开放时间和寄存要求以场馆当日公告为准。',40);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(472,1015,5,1,'REAL_NAME_POLICY','实名制说明','本项目实行实名制购票，一张票对应一名观演人，入场人与购票时填写的观演人须一致。',50);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(473,1015,7,1,'PROHIBITED_ITEMS','禁止携带物品说明','禁止携带易燃易爆物品、管制器具、专业摄影摄像设备及其他影响现场安全或版权管理的物品。',60);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(474,1015,2,1,'ENTRY_NOTICE','入场说明','Provider 入场须知发生变化，重新同步后应更新 Provider 来源内容。',70);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(475,1015,12,1,'E_TICKET_NOTICE','电子票说明','电子票生成后请在票夹中查看；静态码或动态码类型以第三方实际出票结果为准。',80);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(476,1015,13,1,'SEAT_SELECTION_NOTICE','选座说明','当前分销链路不支持用户选座，座位信息以第三方出票结果为准。',90);
insert  into `project_notice_rel`(`rel_id`,`project_id`,`notice_id`,`source_provider_id`,`provider_notice_key`,`title_override`,`description_override`,`sort_order`) values 
(477,1015,6,1,'REFUND_NOTICE','退票政策','本项目执行条件退，退款事实由 Provider 同步。',100);

/*Table structure for table `project_service_tag_rel` */

DROP TABLE IF EXISTS `project_service_tag_rel`;

CREATE TABLE `project_service_tag_rel` (
  `rel_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '关联主键',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '项目ID',
  `tag_id` bigint(20) unsigned NOT NULL COMMENT '服务标签ID',
  `source_provider_id` bigint(20) unsigned DEFAULT NULL COMMENT '第三方来源；空表示本地运营',
  `sort_order` int(10) unsigned NOT NULL COMMENT '项目下展示顺序',
  PRIMARY KEY (`rel_id`),
  UNIQUE KEY `uk_project_service_tag` (`project_id`,`tag_id`),
  KEY `idx_project_service_tag_sort` (`project_id`,`sort_order`),
  KEY `idx_project_service_tag_tag` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=587 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目服务标签关联';

/*Data for the table `project_service_tag_rel` */

insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(219,190003,1,1,2);
insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(220,190003,3,1,3);
insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(478,300003,1,NULL,1);
insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(564,1015,2,1,1);
insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(565,1015,1,1,2);
insert  into `project_service_tag_rel`(`rel_id`,`project_id`,`tag_id`,`source_provider_id`,`sort_order`) values 
(566,1015,3,1,3);

/*Table structure for table `push_business_state` */

DROP TABLE IF EXISTS `push_business_state`;

CREATE TABLE `push_business_state` (
  `state_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `entity_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state_value` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `state_revision` int(10) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`state_id`),
  UNIQUE KEY `uk_push_business_entity` (`entity_type`,`entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `push_business_state` */

/*Table structure for table `push_device_binding` */

DROP TABLE IF EXISTS `push_device_binding`;

CREATE TABLE `push_device_binding` (
  `binding_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `device_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `push_token` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `binding_status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `invalid_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_seen_time` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`binding_id`),
  UNIQUE KEY `uk_push_device` (`device_id`),
  KEY `idx_push_user_status` (`user_id`,`binding_status`),
  KEY `idx_push_token` (`push_token`(191)),
  KEY `idx_push_cleanup` (`binding_status`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `push_device_binding` */

/*Table structure for table `push_notification_delivery` */

DROP TABLE IF EXISTS `push_notification_delivery`;

CREATE TABLE `push_notification_delivery` (
  `delivery_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `event_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(48) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `binding_id` bigint(20) unsigned NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` bigint(20) unsigned DEFAULT NULL,
  `ticket_id` bigint(20) unsigned DEFAULT NULL,
  `project_id` bigint(20) unsigned DEFAULT NULL,
  `session_id` bigint(20) unsigned DEFAULT NULL,
  `delivery_status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `retry_count` int(10) unsigned NOT NULL DEFAULT '0',
  `next_retry_time` datetime DEFAULT NULL,
  `provider_request_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sent_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`delivery_id`),
  UNIQUE KEY `uk_push_event_binding` (`event_key`,`binding_id`),
  KEY `idx_push_delivery_due` (`delivery_status`,`next_retry_time`),
  KEY `idx_push_delivery_user` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `push_notification_delivery` */

/*Table structure for table `refund_record` */

DROP TABLE IF EXISTS `refund_record`;

CREATE TABLE `refund_record` (
  `refund_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '退款记录主键',
  `refund_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款编号',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '所属订单',
  `refund_rule_id` bigint(20) unsigned DEFAULT NULL COMMENT '命中的退款规则，可空',
  `matched_stage_id` bigint(20) unsigned DEFAULT NULL COMMENT '命中的阶梯，可空',
  `refund_type_snapshot` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请时退款类型快照',
  `fee_rate_snapshot` decimal(5,4) DEFAULT NULL COMMENT '申请时手续费比例快照，可空',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因，可空',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '实际退款金额快照',
  `fee_amount` decimal(10,2) NOT NULL COMMENT '手续费金额快照',
  `refund_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款状态',
  `refund_time` datetime DEFAULT NULL COMMENT '退款成功时间，可空',
  `fail_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因，可空',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`refund_id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  UNIQUE KEY `uk_refund_order` (`order_id`),
  KEY `idx_refund_status` (`refund_status`),
  KEY `idx_refund_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录';

/*Data for the table `refund_record` */

/*Table structure for table `refund_rule` */

DROP TABLE IF EXISTS `refund_rule`;

CREATE TABLE `refund_rule` (
  `refund_rule_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '退款规则主键',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '适用演出项目ID',
  `source_provider_id` bigint(20) unsigned DEFAULT NULL COMMENT '规则来源；空表示本地运营',
  `provider_rule_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方退款规则ID',
  `refund_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款类型：条件退/不可退',
  `consumer_entry_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否开放用户退款入口',
  `delivery_fee_refundable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '快递费是否可退',
  `rule_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款规则展示摘要',
  `create_time` datetime NOT NULL COMMENT '规则创建时间',
  `update_time` datetime NOT NULL COMMENT '规则最后更新时间',
  PRIMARY KEY (`refund_rule_id`),
  UNIQUE KEY `uk_refund_rule_project` (`project_id`),
  KEY `idx_refund_rule_type` (`refund_type`)
) ENGINE=InnoDB AUTO_INCREMENT=3056 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款规则主表';

/*Data for the table `refund_rule` */

insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3015,1015,1,'MOCK-PROJ-1001:REFUND','CONDITIONAL_REFUND',1,0,'距离开演7天以上免费退；7天以内且超过24小时收取15%手续费；24小时内不可退。','2026-08-06 22:36:53','2026-08-25 09:57:58');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3017,190003,1,'MOCK-PROJ-1003:REFUND','CONDITIONAL_REFUND',1,1,'演出开始前7天免费退；7天至24小时前收取10%；24小时内不可退。','2026-08-10 10:50:11','2026-08-12 10:01:38');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3018,300001,1,'DEMO-MOCK-PROJ-001:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3019,300002,1,'DEMO-MOCK-PROJ-002:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3020,300003,1,'DEMO-MOCK-PROJ-003:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3021,300004,1,'DEMO-MOCK-PROJ-004:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3022,300005,1,'DEMO-MOCK-PROJ-005:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3023,300006,1,'DEMO-MOCK-PROJ-006:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3024,300007,1,'DEMO-MOCK-PROJ-007:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');
insert  into `refund_rule`(`refund_rule_id`,`project_id`,`source_provider_id`,`provider_rule_id`,`refund_type`,`consumer_entry_enabled`,`delivery_fee_refundable`,`rule_description`,`create_time`,`update_time`) values 
(3025,300008,1,'DEMO-MOCK-PROJ-008:REFUND','NO_REFUND',0,0,'Mock 大麦项目默认不可退规则：票品支付成功后不支持主动退换，具体以项目展示规则为准。','2026-08-23 13:17:08','2026-08-23 13:17:08');

/*Table structure for table `refund_rule_stage` */

DROP TABLE IF EXISTS `refund_rule_stage`;

CREATE TABLE `refund_rule_stage` (
  `stage_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '阶梯规则主键',
  `refund_rule_id` bigint(20) unsigned NOT NULL COMMENT '所属退款规则ID',
  `min_before_start_minutes` int(10) unsigned NOT NULL COMMENT '距离开演至少多少分钟，包含该值',
  `max_before_start_minutes` int(10) unsigned DEFAULT NULL COMMENT '距离开演小于多少分钟，空表示无上限',
  `stage_result` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'FEE_PERCENT' COMMENT 'FREE/FEE_PERCENT/FEE_FIXED/NOT_ALLOWED',
  `fee_rate` decimal(5,4) DEFAULT NULL COMMENT '比例手续费；非比例阶梯可空',
  `fixed_fee_amount` decimal(10,2) DEFAULT NULL COMMENT '固定手续费金额',
  `sort_order` int(10) unsigned NOT NULL COMMENT '展示排序',
  PRIMARY KEY (`stage_id`),
  UNIQUE KEY `uk_refund_stage_sort` (`refund_rule_id`,`sort_order`),
  KEY `idx_refund_stage_rule_min` (`refund_rule_id`,`min_before_start_minutes`)
) ENGINE=InnoDB AUTO_INCREMENT=3407 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='条件退阶梯规则';

/*Data for the table `refund_rule_stage` */

insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3213,3017,10080,NULL,'FREE',NULL,NULL,1);
insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3214,3017,1440,10080,'FEE_PERCENT',0.1000,NULL,2);
insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3215,3017,0,1440,'NOT_ALLOWED',NULL,NULL,3);
insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3404,3015,10080,NULL,'FREE',NULL,NULL,1);
insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3405,3015,1440,10080,'FEE_PERCENT',0.1500,NULL,2);
insert  into `refund_rule_stage`(`stage_id`,`refund_rule_id`,`min_before_start_minutes`,`max_before_start_minutes`,`stage_result`,`fee_rate`,`fixed_fee_amount`,`sort_order`) values 
(3406,3015,0,1440,'NOT_ALLOWED',NULL,NULL,3);

/*Table structure for table `search_history` */

DROP TABLE IF EXISTS `search_history`;

CREATE TABLE `search_history` (
  `history_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '搜索历史主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '所属用户',
  `keyword` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `last_search_time` datetime NOT NULL COMMENT '最近一次搜索时间',
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `uk_search_user_keyword` (`user_id`,`keyword`),
  KEY `idx_search_user_time` (`user_id`,`last_search_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索历史';

/*Data for the table `search_history` */

/*Table structure for table `service_card_binding` */

DROP TABLE IF EXISTS `service_card_binding`;

CREATE TABLE `service_card_binding` (
  `service_card_binding_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `binding_id` bigint(20) NOT NULL,
  `form_id` varchar(32) NOT NULL,
  `module_name` varchar(128) NOT NULL,
  `ability_name` varchar(128) NOT NULL,
  `form_name` varchar(128) NOT NULL,
  `city_name` varchar(64) NOT NULL DEFAULT '北京',
  `last_push_version` bigint(20) NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`service_card_binding_id`),
  UNIQUE KEY `uk_service_card_form_id` (`form_id`),
  KEY `idx_service_card_binding_id` (`binding_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Data for the table `service_card_binding` */

/*Table structure for table `service_tag` */

DROP TABLE IF EXISTS `service_tag`;

CREATE TABLE `service_tag` (
  `tag_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '服务标签主键',
  `tag_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签说明',
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_service_tag_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务标签字典';

/*Data for the table `service_tag` */

insert  into `service_tag`(`tag_id`,`tag_name`,`description`) values 
(1,'实名制购票','一票一证入场');
insert  into `service_tag`(`tag_id`,`tag_name`,`description`) values 
(2,'电子票','支付成功后生成电子票，入场时出示二维码核验。');
insert  into `service_tag`(`tag_id`,`tag_name`,`description`) values 
(3,'纸质票','本项目支持纸质票或快递票，配送信息以下单页为准。');
insert  into `service_tag`(`tag_id`,`tag_name`,`description`) values 
(4,'条件退','退款规则以本项目实际展示为准，点击后可查看当前项目的分阶段退票规则。');
insert  into `service_tag`(`tag_id`,`tag_name`,`description`) values 
(5,'不可退','本项目不支持用户主动退票，具体规则以项目退款政策为准。');

/*Table structure for table `service_tag_capability_rel` */

DROP TABLE IF EXISTS `service_tag_capability_rel`;

CREATE TABLE `service_tag_capability_rel` (
  `capability_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'V1.1统一能力码',
  `tag_id` bigint(20) unsigned NOT NULL COMMENT '本地服务标签ID',
  PRIMARY KEY (`capability_code`),
  KEY `idx_service_capability_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一服务能力码与本地标签映射';

/*Data for the table `service_tag_capability_rel` */

insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('ONE_TICKET_ONE_ID',1);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('REAL_NAME_ENTRY',1);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('REAL_NAME_PURCHASE',1);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('DYNAMIC_QR',2);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('E_TICKET',2);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('STATIC_QR',2);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('EXPRESS_DELIVERY',3);
insert  into `service_tag_capability_rel`(`capability_code`,`tag_id`) values 
('PAPER_TICKET',3);

/*Table structure for table `ticket_operation_log` */

DROP TABLE IF EXISTS `ticket_operation_log`;

CREATE TABLE `ticket_operation_log` (
  `log_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `log_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志编号',
  `business_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务维度：TICKET/ISSUE/CHECK/QR/REFUND',
  `action_type` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '具体动作类型',
  `operator_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行者维度：SYSTEM/ADMIN',
  `operator_id` bigint(20) unsigned DEFAULT NULL COMMENT '管理员ID，系统执行时为空',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执行者名称快照',
  `source_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作来源IP',
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：TICKET/ORDER',
  `target_id` bigint(20) unsigned DEFAULT NULL COMMENT '目标主键',
  `order_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联订单ID',
  `ticket_id` bigint(20) unsigned DEFAULT NULL COMMENT '关联电子票ID',
  `before_status` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作前状态',
  `after_status` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作后状态',
  `result_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SUCCESS/FAILED',
  `action_description` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作说明',
  `detail_json` text COLLATE utf8mb4_unicode_ci COMMENT '扩展详情JSON',
  `create_time` datetime NOT NULL COMMENT '发生时间',
  PRIMARY KEY (`log_id`),
  UNIQUE KEY `uk_ticket_operation_log_no` (`log_no`),
  KEY `idx_ticket_log_business_time` (`business_type`,`create_time`),
  KEY `idx_ticket_log_operator_time` (`operator_type`,`create_time`),
  KEY `idx_ticket_log_action_time` (`action_type`,`create_time`),
  KEY `idx_ticket_log_order_id` (`order_id`),
  KEY `idx_ticket_log_ticket_id` (`ticket_id`),
  KEY `idx_ticket_log_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票务业务与管理员操作统一日志';

/*Data for the table `ticket_operation_log` */

/*Table structure for table `ticket_order` */

DROP TABLE IF EXISTS `ticket_order`;

CREATE TABLE `ticket_order` (
  `order_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单主键',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '对用户可见的订单编号',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '下单用户ID',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '演出项目ID',
  `session_id` bigint(20) unsigned NOT NULL COMMENT '场次ID',
  `order_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单状态',
  `delivery_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配送方式',
  `fulfillment_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_COMPAT' COMMENT '履约模式：LOCAL_COMPAT/TICKET_SOURCE',
  `ticket_amount` decimal(10,2) NOT NULL COMMENT '票款金额',
  `service_fee_amount` decimal(10,2) NOT NULL COMMENT '服务费',
  `delivery_fee_amount` decimal(10,2) NOT NULL COMMENT '配送费',
  `discount_amount` decimal(10,2) NOT NULL COMMENT '优惠金额，一期可为0',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `payment_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID/PAID/PROVIDER_CONFIRMED/REFUNDED',
  `pay_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
  `pay_expire_time` datetime DEFAULT NULL COMMENT '支付截止时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付成功时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `ticket_issued_time` datetime DEFAULT NULL COMMENT '实际出票时间',
  `finish_time` datetime DEFAULT NULL COMMENT '订单完成时间',
  `create_time` datetime NOT NULL COMMENT '下单时间',
  `update_time` datetime NOT NULL COMMENT '订单最后更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user_time` (`user_id`,`create_time`),
  KEY `idx_order_user_status_time` (`user_id`,`order_status`,`create_time`),
  KEY `idx_order_project` (`project_id`),
  KEY `idx_order_session` (`session_id`),
  KEY `idx_order_pay_timeout` (`order_status`,`pay_expire_time`),
  KEY `idx_order_fulfillment_payment` (`fulfillment_mode`,`payment_status`,`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

/*Data for the table `ticket_order` */

/*Table structure for table `ticket_sku` */

DROP TABLE IF EXISTS `ticket_sku`;

CREATE TABLE `ticket_sku` (
  `sku_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '票档主键',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '所属项目',
  `session_id` bigint(20) unsigned NOT NULL COMMENT '所属场次',
  `sku_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '票档名称',
  `sku_desc` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '票档描述，可空',
  `price` decimal(10,2) NOT NULL COMMENT '单张票价',
  `price_mode` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_MANAGED' COMMENT '平台售价策略: LOCAL_MANAGED/FOLLOW_PROVIDER/FIXED',
  `stock_available` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '可售库存',
  `stock_locked` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '锁定库存',
  `sold_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '已售数量',
  `sku_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '票档状态',
  `inventory_authority` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOCAL_COMPAT' COMMENT '库存权威来源：LOCAL_COMPAT/PROVIDER_SNAPSHOT/PROVIDER_REALTIME',
  `sort_order` int(10) unsigned DEFAULT NULL COMMENT '展示排序',
  `version` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '库存并发版本号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`sku_id`),
  KEY `idx_sku_project` (`project_id`),
  KEY `idx_sku_session_status_sort` (`session_id`,`sku_status`,`sort_order`),
  KEY `idx_sku_inventory_authority` (`inventory_authority`)
) ENGINE=InnoDB AUTO_INCREMENT=590217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票档/票品';

/*Data for the table `ticket_sku` */

insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(22024,1015,12015,'票档 ¥366','第三方票源：MOCK-SKU-21001',366.00,'FOLLOW_PROVIDER',65,0,0,'ON_SALE','PROVIDER_REALTIME',1,139,'2026-08-03 00:47:29','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(22025,1015,12015,'单日 VIP 880','第三方票源：MOCK-SKU-21002',880.00,'FOLLOW_PROVIDER',32,0,0,'ON_SALE','PROVIDER_REALTIME',2,86,'2026-08-03 00:47:29','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(22030,1015,12015,'纪念纸质票 1280','第三方票源：MOCK-SKU-21007',1280.00,'FOLLOW_PROVIDER',22,0,0,'ON_SALE','PROVIDER_REALTIME',3,78,'2026-08-06 22:26:07','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(290003,190003,190003,'一楼普通席 380（纸质票）','第三方票源：MOCK-SKU-22001',380.00,'FOLLOW_PROVIDER',26,0,0,'ON_SALE','PROVIDER_REALTIME',1,5,'2026-08-10 10:50:11','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(290004,190003,190003,'一楼前排席 680（纸质票·未知库存）','第三方票源：MOCK-SKU-22002',680.00,'FOLLOW_PROVIDER',0,0,0,'ON_SALE','PROVIDER_SNAPSHOT',2,1,'2026-08-10 10:50:11','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500001,300001,400001,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',49,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-20 21:25:22');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500002,300001,400001,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',19,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-20 21:25:22');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500003,300002,400002,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',50,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-23 10:51:08');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500004,300002,400002,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',20,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-23 10:51:08');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500005,300003,400003,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',51,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-23 10:51:08');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500006,300003,400003,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',21,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-23 10:51:08');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500007,300004,400004,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',52,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-23 19:31:09');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500008,300004,400004,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',22,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-23 19:31:09');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500009,300005,400005,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',53,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-24 22:44:26');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500010,300005,400005,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',23,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-24 22:44:26');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500011,300006,400006,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',54,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-25 22:30:12');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500012,300006,400006,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',24,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-25 22:30:12');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500013,300007,400007,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',55,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-26 23:16:22');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500014,300007,400007,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',18,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-26 23:16:22');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500015,300008,400008,'普通票 280','电子票档',280.00,'FOLLOW_PROVIDER',54,0,0,'SOLD_OUT','PROVIDER_REALTIME',1,0,'2026-08-17 13:22:32','2026-08-28 00:58:05');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(500016,300008,400008,'优享票 1280','电子票档',1280.00,'FOLLOW_PROVIDER',19,0,0,'SOLD_OUT','PROVIDER_REALTIME',2,0,'2026-08-17 13:22:32','2026-08-28 00:58:05');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590201,390101,490101,'普通票 280','第三方票源票档',280.00,'FOLLOW_PROVIDER',60,0,0,'SOLD_OUT','PROVIDER_SNAPSHOT',1,0,'2026-08-30 17:28:13','2026-09-02 19:40:14');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590202,390101,490101,'VIP票 1280','第三方票源票档',1280.00,'FOLLOW_PROVIDER',20,0,0,'SOLD_OUT','PROVIDER_SNAPSHOT',2,0,'2026-08-30 17:28:13','2026-09-02 19:40:14');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590203,390102,490102,'普通票 180','第三方票源票档',180.00,'FOLLOW_PROVIDER',70,0,0,'ON_SALE','PROVIDER_SNAPSHOT',1,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590204,390102,490102,'优享票 680','第三方票源票档',680.00,'FOLLOW_PROVIDER',25,0,0,'ON_SALE','PROVIDER_SNAPSHOT',2,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590205,390103,490103,'普通票 380','第三方票源票档',380.00,'FOLLOW_PROVIDER',50,0,0,'ON_SALE','PROVIDER_SNAPSHOT',1,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590206,390103,490103,'VIP票 1580','第三方票源票档',1580.00,'FOLLOW_PROVIDER',18,0,0,'ON_SALE','PROVIDER_SNAPSHOT',2,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590207,390104,490104,'普通票 280','第三方票源票档',280.00,'FOLLOW_PROVIDER',65,0,0,'ON_SALE','PROVIDER_SNAPSHOT',1,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');
insert  into `ticket_sku`(`sku_id`,`project_id`,`session_id`,`sku_name`,`sku_desc`,`price`,`price_mode`,`stock_available`,`stock_locked`,`sold_count`,`sku_status`,`inventory_authority`,`sort_order`,`version`,`create_time`,`update_time`) values 
(590208,390104,490104,'VIP票 1280','第三方票源票档',1280.00,'FOLLOW_PROVIDER',21,0,0,'ON_SALE','PROVIDER_SNAPSHOT',2,0,'2026-08-30 17:28:13','2026-09-03 13:38:59');

/*Table structure for table `ticket_source_callback_event` */

DROP TABLE IF EXISTS `ticket_source_callback_event`;

CREATE TABLE `ticket_source_callback_event` (
  `event_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '本地事件主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `provider_event_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方事件ID，用于幂等',
  `event_type` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PROJECT_CHANGED/INVENTORY_CHANGED/TICKET_ISSUED/REFUND_CHANGED/SHIPMENT_CHANGED等',
  `resource_type` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_resource_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方资源ID',
  `event_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方事件或资源版本',
  `occurred_time` datetime DEFAULT NULL COMMENT '第三方事件发生时间',
  `request_timestamp` bigint(20) DEFAULT NULL,
  `request_nonce` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `signature_valid` tinyint(1) NOT NULL DEFAULT '0',
  `process_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/SUCCESS/FAILED',
  `retry_count` int(10) unsigned NOT NULL DEFAULT '0',
  `next_attempt_time` datetime DEFAULT NULL,
  `processed_time` datetime DEFAULT NULL COMMENT '处理完成时间',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近处理错误',
  `payload_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '验签后的原始事件快照',
  `create_time` datetime NOT NULL COMMENT '接收时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`event_id`),
  UNIQUE KEY `uk_source_callback_event` (`provider_id`,`provider_event_id`),
  UNIQUE KEY `uk_source_callback_nonce` (`provider_id`,`request_nonce`),
  KEY `idx_source_callback_process` (`process_status`,`create_time`),
  KEY `idx_source_callback_resource` (`provider_id`,`event_type`,`provider_resource_id`),
  KEY `idx_source_callback_due` (`process_status`,`next_attempt_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方回调事件收件箱';

/*Data for the table `ticket_source_callback_event` */

/*Table structure for table `ticket_source_campaign_asset` */

DROP TABLE IF EXISTS `ticket_source_campaign_asset`;

CREATE TABLE `ticket_source_campaign_asset` (
  `asset_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '第三方活动素材主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `provider_asset_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方素材ID',
  `asset_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BANNER/CARD/POSTER/OTHER',
  `position_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方建议投放位置',
  `title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '素材标题',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动文案',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '素材图片',
  `mobile_image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '移动端素材图片',
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PROJECT/SESSION/CATEGORY/URL',
  `provider_target_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方目标ID或URL',
  `city_codes` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适用城市编码，逗号分隔',
  `start_time` datetime DEFAULT NULL COMMENT '建议投放开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '建议投放结束时间',
  `provider_promotion_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联第三方优惠规则ID',
  `review_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `review_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '麦麦运营审核备注',
  `review_time` datetime DEFAULT NULL COMMENT '麦麦运营最后审核时间',
  `banner_id` bigint(20) unsigned DEFAULT NULL COMMENT '审核通过后生成的本地Banner ID',
  `source_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '第三方当前是否仍提供该素材',
  `source_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本',
  `source_updated_time` datetime DEFAULT NULL COMMENT '第三方更新时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `uk_source_campaign_remote` (`provider_id`,`provider_asset_id`),
  KEY `idx_source_campaign_review` (`review_status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方活动素材待审核池';

/*Data for the table `ticket_source_campaign_asset` */

insert  into `ticket_source_campaign_asset`(`asset_id`,`provider_id`,`provider_asset_id`,`asset_type`,`position_code`,`title`,`description`,`image_url`,`mobile_image_url`,`target_type`,`provider_target_id`,`city_codes`,`start_time`,`end_time`,`provider_promotion_id`,`review_status`,`review_remark`,`review_time`,`banner_id`,`source_enabled`,`source_version`,`source_updated_time`,`create_time`,`update_time`) values 
(1,1,'H3R3R3-CAMPAIGN-001','BANNER','HOME_TOP','条件退演出推荐','活动素材待审核池演示：300034 条件退演出首页 Banner。','/media/banner/image/2026/08/20260818024518_b7e6ed1c_长方图.jpeg','/media/banner/image/2026/08/20260818024518_b7e6ed1c_长方图.jpeg','PROJECT','DEMO-MOCK-PROJ-034','110100','2026-08-24 00:00:00','2026-12-31 23:59:59',NULL,'PENDING',NULL,NULL,NULL,1,'H3R3R3-CAMPAIGN-001-v1','2026-08-24 11:40:55','2026-08-24 11:40:55','2026-08-24 11:40:55');
insert  into `ticket_source_campaign_asset`(`asset_id`,`provider_id`,`provider_asset_id`,`asset_type`,`position_code`,`title`,`description`,`image_url`,`mobile_image_url`,`target_type`,`provider_target_id`,`city_codes`,`start_time`,`end_time`,`provider_promotion_id`,`review_status`,`review_remark`,`review_time`,`banner_id`,`source_enabled`,`source_version`,`source_updated_time`,`create_time`,`update_time`) values 
(2,1,'H3R3R3-CAMPAIGN-002','BANNER','HOME_TOP','限购两张项目推荐','活动素材待审核池演示：300035 限购两张项目。','/media/banner/image/2026/08/20260801182957_fd9026ef_third-banner.png','/media/banner/image/2026/08/20260801182957_fd9026ef_third-banner.png','PROJECT','DEMO-MOCK-PROJ-035','110100','2026-08-24 00:00:00','2026-12-31 23:59:59',NULL,'PENDING',NULL,NULL,NULL,1,'H3R3R3-CAMPAIGN-002-v1','2026-08-24 11:40:55','2026-08-24 11:40:55','2026-08-24 11:40:55');
insert  into `ticket_source_campaign_asset`(`asset_id`,`provider_id`,`provider_asset_id`,`asset_type`,`position_code`,`title`,`description`,`image_url`,`mobile_image_url`,`target_type`,`provider_target_id`,`city_codes`,`start_time`,`end_time`,`provider_promotion_id`,`review_status`,`review_remark`,`review_time`,`banner_id`,`source_enabled`,`source_version`,`source_updated_time`,`create_time`,`update_time`) values 
(3,1,'H3R3R3-CAMPAIGN-003','CARD','HOME_RECOMMEND','限购四张项目卡片','活动素材待审核池演示：300036 推荐卡片。','/media/project/detail-image/2026/08/20260823114618_1a689d28_1731068382110_nEY2.jpg','/media/project/detail-image/2026/08/20260823114618_1a689d28_1731068382110_nEY2.jpg','PROJECT','DEMO-MOCK-PROJ-036','110100','2026-08-24 00:00:00','2026-12-31 23:59:59',NULL,'PENDING',NULL,NULL,NULL,1,'H3R3R3-CAMPAIGN-003-v1','2026-08-24 11:40:55','2026-08-24 11:40:55','2026-08-24 11:40:55');
insert  into `ticket_source_campaign_asset`(`asset_id`,`provider_id`,`provider_asset_id`,`asset_type`,`position_code`,`title`,`description`,`image_url`,`mobile_image_url`,`target_type`,`provider_target_id`,`city_codes`,`start_time`,`end_time`,`provider_promotion_id`,`review_status`,`review_remark`,`review_time`,`banner_id`,`source_enabled`,`source_version`,`source_updated_time`,`create_time`,`update_time`) values 
(4,1,'H3R3R3-CAMPAIGN-004','POSTER','PROJECT_DETAIL','限购六张项目海报','活动素材待审核池演示：300037 项目海报素材。','/media/project/detail-image/2026/08/20260823184303_e6f66c19_长方图.jpeg','/media/project/detail-image/2026/08/20260823184303_e6f66c19_长方图.jpeg','PROJECT','DEMO-MOCK-PROJ-037','110100','2026-08-24 00:00:00','2026-12-31 23:59:59',NULL,'PENDING',NULL,NULL,NULL,1,'H3R3R3-CAMPAIGN-004-v1','2026-08-24 11:40:55','2026-08-24 11:40:55','2026-08-24 11:40:55');
insert  into `ticket_source_campaign_asset`(`asset_id`,`provider_id`,`provider_asset_id`,`asset_type`,`position_code`,`title`,`description`,`image_url`,`mobile_image_url`,`target_type`,`provider_target_id`,`city_codes`,`start_time`,`end_time`,`provider_promotion_id`,`review_status`,`review_remark`,`review_time`,`banner_id`,`source_enabled`,`source_version`,`source_updated_time`,`create_time`,`update_time`) values 
(5,1,'H3R3R3-CAMPAIGN-005','BANNER','CITY_HOME','城市活动专题推荐','活动素材待审核池演示：独立待审核 Banner，可审核后选择本地演出生成 Banner。','/media/banner/image/2026/08/20260818024518_b7e6ed1c_长方图.jpeg','/media/banner/image/2026/08/20260818024518_b7e6ed1c_长方图.jpeg','PROJECT','DEMO-MOCK-PROJ-036','110100','2026-08-24 00:00:00','2026-11-30 23:59:59',NULL,'APPROVED','后台审核通过','2026-08-24 11:41:50',9,1,'H3R3R3-CAMPAIGN-005-v1','2026-08-24 11:40:55','2026-08-24 11:40:55','2026-08-24 11:42:29');

/*Table structure for table `ticket_source_delivery_fee_config` */

DROP TABLE IF EXISTS `ticket_source_delivery_fee_config`;

CREATE TABLE `ticket_source_delivery_fee_config` (
  `config_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '配送费配置ID',
  `provider_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '票源供应商编码',
  `delivery_method` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'EXPRESS/SELF_PICKUP等',
  `amount_minor` bigint(20) NOT NULL COMMENT '配送费，最小货币单位，人民币为分',
  `currency` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  KEY `idx_delivery_fee_provider_method` (`provider_code`,`delivery_method`,`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源用户侧配送费配置';

/*Data for the table `ticket_source_delivery_fee_config` */

insert  into `ticket_source_delivery_fee_config`(`config_id`,`provider_code`,`delivery_method`,`amount_minor`,`currency`,`enabled`,`remark`,`create_time`,`update_time`) values 
(1,'MOCK_DAMAI','EXPRESS',1200,'CNY',1,'DEMO baseline','2026-08-09 09:03:29','2026-08-11 18:48:50');

/*Table structure for table `ticket_source_gateway_log` */

DROP TABLE IF EXISTS `ticket_source_gateway_log`;

CREATE TABLE `ticket_source_gateway_log` (
  `log_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `request_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adapter_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `operation_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `success` tinyint(1) NOT NULL,
  `gateway_error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `retryable` tinyint(1) NOT NULL DEFAULT '0',
  `elapsed_ms` bigint(20) unsigned NOT NULL DEFAULT '0',
  `call_time` datetime NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`log_id`),
  UNIQUE KEY `uk_gateway_log_request` (`request_id`),
  KEY `idx_gateway_log_provider_operation` (`provider_code`,`operation_code`,`call_time`),
  KEY `idx_gateway_log_success_time` (`success`,`call_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一票源网关调用日志';

/*Data for the table `ticket_source_gateway_log` */

/*Table structure for table `ticket_source_issue_task` */

DROP TABLE IF EXISTS `ticket_source_issue_task`;

CREATE TABLE `ticket_source_issue_task` (
  `task_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) unsigned NOT NULL,
  `bridge_id` bigint(20) unsigned NOT NULL,
  `provider_id` bigint(20) unsigned NOT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `task_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PENDING/PROCESSING/WAIT_PROVIDER/RETRY_WAIT/PARTIAL/SUCCESS/FAILED/MANUAL_REVIEW',
  `provider_delivery_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN',
  `expected_ticket_count` int(10) unsigned NOT NULL,
  `issued_count` int(10) unsigned NOT NULL DEFAULT '0',
  `failed_count` int(10) unsigned NOT NULL DEFAULT '0',
  `retry_count` int(10) unsigned NOT NULL DEFAULT '0',
  `max_retry_count` int(10) unsigned NOT NULL DEFAULT '5',
  `issue_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `request_sent` tinyint(1) NOT NULL DEFAULT '0',
  `manual_hold` tinyint(1) NOT NULL DEFAULT '0',
  `next_attempt_time` datetime DEFAULT NULL,
  `last_attempt_time` datetime DEFAULT NULL,
  `complete_time` datetime DEFAULT NULL,
  `provider_delivery_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_operation` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_retryable` tinyint(1) NOT NULL DEFAULT '0',
  `version` int(10) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`task_id`),
  UNIQUE KEY `uk_issue_task_order` (`order_id`),
  UNIQUE KEY `uk_issue_task_bridge` (`bridge_id`),
  UNIQUE KEY `uk_issue_task_idempotency` (`issue_idempotency_key`),
  KEY `idx_issue_task_due` (`manual_hold`,`task_status`,`next_attempt_time`),
  KEY `idx_issue_task_provider_order` (`provider_id`,`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源自动出票任务';

/*Data for the table `ticket_source_issue_task` */

/*Table structure for table `ticket_source_order_bridge` */

DROP TABLE IF EXISTS `ticket_source_order_bridge`;

CREATE TABLE `ticket_source_order_bridge` (
  `bridge_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '第三方订单桥接主键',
  `order_id` bigint(20) unsigned NOT NULL COMMENT '本地订单ID',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `sku_mapping_id` bigint(20) unsigned DEFAULT NULL COMMENT '旧单票档兼容字段',
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方订单ID',
  `provider_order_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方可见订单号',
  `provider_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_model` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SINGLE_SKU' COMMENT 'SINGLE_SKU/MULTI_SKU',
  `quote_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'V1.1服务端计价单ID',
  `provider_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '旧单票档兼容字段',
  `bridge_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'INITIATING/UNKNOWN_RESULT/RESERVED/PAYMENT_CONFIRMING/PAID/CANCELING/CANCELED/EXPIRED/FAILED/MANUAL_REVIEW',
  `provider_order_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/WAIT_PAY/PAID/CANCELED/EXPIRED',
  `quantity` int(10) unsigned DEFAULT NULL COMMENT '旧单票档兼容字段',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '旧单票档兼容字段',
  `pay_amount` decimal(10,2) NOT NULL,
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `create_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cancel_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reservation_expire_time` datetime DEFAULT NULL,
  `provider_create_time` datetime DEFAULT NULL,
  `provider_pay_time` datetime DEFAULT NULL,
  `provider_cancel_time` datetime DEFAULT NULL,
  `last_operation` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_sync_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_retryable` tinyint(1) NOT NULL DEFAULT '0',
  `unknown_result_since` datetime DEFAULT NULL COMMENT 'R5 createOrder结果不确定首次发生时间',
  `create_recovery_attempts` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'R5 createOrder补查次数',
  `last_recovery_time` datetime DEFAULT NULL COMMENT 'R5最近补查时间',
  `request_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '不包含完整实名证件号的请求摘要',
  `response_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '第三方响应摘要',
  `version` int(10) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`bridge_id`),
  UNIQUE KEY `uk_source_order_local` (`order_id`),
  UNIQUE KEY `uk_source_order_create_idempotency` (`create_idempotency_key`),
  UNIQUE KEY `uk_source_order_payment_idempotency` (`payment_idempotency_key`),
  UNIQUE KEY `uk_source_order_cancel_idempotency` (`cancel_idempotency_key`),
  UNIQUE KEY `uk_source_order_provider_remote` (`provider_id`,`provider_order_id`),
  UNIQUE KEY `uk_source_order_quote` (`quote_id`),
  KEY `idx_source_order_status_expire` (`bridge_status`,`reservation_expire_time`),
  KEY `idx_source_order_provider_status` (`provider_id`,`provider_order_status`,`update_time`),
  KEY `idx_source_order_sku_mapping` (`sku_mapping_id`),
  KEY `idx_source_order_unknown_recovery` (`bridge_status`,`last_recovery_time`,`unknown_result_since`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地订单与第三方订单桥接状态';

/*Data for the table `ticket_source_order_bridge` */

/*Table structure for table `ticket_source_order_item_bridge` */

DROP TABLE IF EXISTS `ticket_source_order_item_bridge`;

CREATE TABLE `ticket_source_order_item_bridge` (
  `bridge_item_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '第三方订单项桥接主键',
  `bridge_id` bigint(20) unsigned NOT NULL COMMENT '订单桥接ID',
  `order_item_id` bigint(20) unsigned NOT NULL COMMENT '本地订单项ID',
  `sku_mapping_id` bigint(20) unsigned NOT NULL COMMENT '票档映射ID',
  `provider_order_item_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方订单项ID',
  `provider_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方票档ID',
  `quantity` int(10) unsigned NOT NULL COMMENT '票数',
  `provider_unit_price` decimal(10,2) DEFAULT NULL COMMENT '第三方成交单价',
  `settlement_unit_price` decimal(10,2) DEFAULT NULL COMMENT '第三方结算单价',
  PRIMARY KEY (`bridge_item_id`),
  UNIQUE KEY `uk_source_order_item_local` (`bridge_id`,`order_item_id`),
  UNIQUE KEY `uk_source_order_item_sku` (`bridge_id`,`provider_sku_id`),
  KEY `idx_source_order_item_mapping` (`sku_mapping_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方多票档订单项桥接';

/*Data for the table `ticket_source_order_item_bridge` */

/*Table structure for table `ticket_source_order_quote` */

DROP TABLE IF EXISTS `ticket_source_order_quote`;

CREATE TABLE `ticket_source_order_quote` (
  `quote_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务端计价单ID',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源ID',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '本地项目ID',
  `session_id` bigint(20) unsigned NOT NULL COMMENT '本地场次ID',
  `provider_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方项目ID',
  `provider_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方场次ID',
  `purchase_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '购买方式',
  `ticket_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '凭证类型',
  `delivery_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '履约配送方式',
  `address_id` bigint(20) unsigned DEFAULT NULL COMMENT '快递地址ID',
  `face_amount` decimal(10,2) NOT NULL COMMENT '票面总额',
  `ticket_amount` decimal(10,2) NOT NULL COMMENT '第三方销售票款',
  `provider_ticket_amount` decimal(10,2) DEFAULT NULL COMMENT 'Provider销售票款',
  `provider_discount_amount` decimal(10,2) DEFAULT NULL COMMENT 'Provider侧优惠',
  `provider_pay_amount` decimal(10,2) DEFAULT NULL COMMENT '向Provider确认/桥接的订单应付金额',
  `settlement_amount` decimal(10,2) NOT NULL COMMENT '第三方结算总额',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '第三方交易优惠',
  `delivery_fee_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '第三方运费',
  `service_fee_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '本地服务费',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '最终应付金额',
  `provider_delivery_quote_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方运费报价ID',
  `request_snapshot` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '本地票档与观演人选择快照，不含完整证件号',
  `items_snapshot` longtext COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方确认后的票档计价快照',
  `promotion_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '已应用优惠快照',
  `expire_time` datetime NOT NULL COMMENT '计价单过期时间',
  `used_order_id` bigint(20) unsigned DEFAULT NULL COMMENT '使用该计价单创建的本地订单',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`quote_id`),
  UNIQUE KEY `uk_source_quote_used_order` (`used_order_id`),
  KEY `idx_source_quote_user_expire` (`user_id`,`expire_time`),
  KEY `idx_source_quote_provider_project` (`provider_id`,`provider_project_id`,`provider_session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.1第三方服务端计价单';

/*Data for the table `ticket_source_order_quote` */

/*Table structure for table `ticket_source_project_mapping` */

DROP TABLE IF EXISTS `ticket_source_project_mapping`;

CREATE TABLE `ticket_source_project_mapping` (
  `mapping_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '项目映射主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '本地标准项目ID',
  `provider_project_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方项目ID，按字符串保存以兼容不同平台',
  `provider_project_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方项目名称快照',
  `mapping_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/BOUND/DISABLED/INVALID',
  `source_sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/PRESALE/ON_SALE/SOLD_OUT/OFF_SHELF/ENDED',
  `source_status_value` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态码或状态值',
  `source_status_text` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态说明',
  `source_data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本、ETag或更新时间戳',
  `source_updated_time` datetime DEFAULT NULL COMMENT '第三方资源更新时间，用于拒绝旧版本覆盖',
  `auto_publish_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否按第三方状态和库存自动联动本地上架状态',
  `last_sync_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEVER' COMMENT 'NEVER/SUCCESS/FAILED',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误码',
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误摘要',
  `source_payload_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '最近一次第三方原始响应快照，禁止放凭证和完整身份证号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`mapping_id`),
  UNIQUE KEY `uk_source_project_provider_local` (`provider_id`,`project_id`),
  UNIQUE KEY `uk_source_project_provider_remote` (`provider_id`,`provider_project_id`),
  KEY `idx_source_project_local` (`project_id`),
  KEY `idx_source_project_status_sync` (`mapping_status`,`last_sync_status`,`last_sync_time`),
  KEY `idx_source_project_auto_publish` (`auto_publish_enabled`,`mapping_status`,`last_sync_status`)
) ENGINE=InnoDB AUTO_INCREMENT=700054 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地项目与第三方项目映射';

/*Data for the table `ticket_source_project_mapping` */

insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(1,1,1015,'MOCK-PROJ-1001','DEMO-PROVIDER-同步覆盖标题-1015','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','DEMO-PROJECT-20260825095630','2026-08-25 09:56:30',1,'SUCCESS','2026-08-25 09:57:58',NULL,NULL,NULL,'2026-08-03 00:47:28','2026-08-25 09:57:58');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(3,1,190003,'MOCK-PROJ-1003','舞台剧《纸上星河》北京站（纸质票演示）','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','MOCK-PROJ-1003-demo-v1','2026-08-10 10:47:00',1,'SUCCESS','2026-08-12 10:01:38',NULL,NULL,NULL,'2026-08-10 10:50:11','2026-08-12 10:01:38');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700001,1,300001,'DEMO-MOCK-PROJ-001','「回声计划」2026巡回演唱会北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-001-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700002,1,300002,'DEMO-MOCK-PROJ-002','「银河来信」2026巡回演唱会北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-002-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700003,1,300003,'DEMO-MOCK-PROJ-003','「夏日漫游」2026演唱会北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-003-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700004,1,300004,'DEMO-MOCK-PROJ-004','「城市脉搏」2026 Live 巡演北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-004-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700005,1,300005,'DEMO-MOCK-PROJ-005','「无界声场」2026巡回演唱会北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-005-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700006,1,300006,'DEMO-MOCK-PROJ-006','「向光而行」2026音乐现场北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-006-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700007,1,300007,'DEMO-MOCK-PROJ-007','「时差之外」2026巡回演唱会北京站','BOUND','PRESALE','DEMO_PRESALE','模拟预售','DEMO-MOCK-PROJ-007-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700008,1,300008,'DEMO-MOCK-PROJ-008','「晚风电台」2026演唱会北京站','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-PROJ-008-v1','2026-08-17 13:22:32',0,'SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700039,1,390101,'DEMO-AI-P-390101','「海上回声」2026巡回演唱会上海站','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-P-390101-v1','2026-08-31 11:54:19',0,'SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700040,1,390102,'DEMO-AI-P-390102','话剧《梧桐夜话》上海站','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-P-390102-v1','2026-08-31 11:54:19',0,'SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700041,1,390103,'DEMO-AI-P-390103','「城市潮汐」2026演唱会上海站','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-P-390103-v1','2026-08-31 11:54:19',0,'SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_project_mapping`(`mapping_id`,`provider_id`,`project_id`,`provider_project_id`,`provider_project_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`auto_publish_enabled`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(700042,1,390104,'DEMO-AI-P-390104','「珠江夜航」2026巡回演唱会广州站','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-P-390104-v1','2026-08-31 11:54:19',0,'SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');

/*Table structure for table `ticket_source_promotion_rule` */

DROP TABLE IF EXISTS `ticket_source_promotion_rule`;

CREATE TABLE `ticket_source_promotion_rule` (
  `promotion_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '优惠规则主键',
  `project_mapping_id` bigint(20) unsigned NOT NULL COMMENT '第三方项目映射ID',
  `provider_promotion_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方优惠规则ID',
  `promotion_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠类型',
  `promotion_title` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '展示标题',
  `promotion_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '展示说明',
  `stackable` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否可与其他优惠叠加',
  `target_scope_json` text COLLATE utf8mb4_unicode_ci COMMENT '适用项目/场次/票档范围JSON',
  `rule_data_json` text COLLATE utf8mb4_unicode_ci COMMENT '第三方交易计价规则JSON',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `promotion_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
  `source_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本',
  `source_updated_time` datetime DEFAULT NULL COMMENT '第三方更新时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`promotion_id`),
  UNIQUE KEY `uk_source_promotion_remote` (`project_mapping_id`,`provider_promotion_id`),
  KEY `idx_source_promotion_effective` (`project_mapping_id`,`promotion_status`,`start_time`,`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方交易优惠规则摘要';

/*Data for the table `ticket_source_promotion_rule` */

insert  into `ticket_source_promotion_rule`(`promotion_id`,`project_mapping_id`,`provider_promotion_id`,`promotion_type`,`promotion_title`,`promotion_description`,`stackable`,`target_scope_json`,`rule_data_json`,`start_time`,`end_time`,`promotion_status`,`source_version`,`source_updated_time`,`update_time`) values 
(6,1,'MOCK-PROMO-1001','FULL_REDUCTION','满1000减50','同一订单票款满1000元减50元',0,'{\"projectIds\":[\"MOCK-PROJ-1001\"],\"ticketProductIds\":[],\"sessionIds\":[]}','{\"thresholdAmountMinor\":100000,\"discountAmountMinor\":5000}','2026-08-01 00:00:00','2026-09-20 13:00:00','DISABLED','mock-promo-1001-v1','2026-08-06 17:25:57','2026-08-25 09:57:58');

/*Table structure for table `ticket_source_provider` */

DROP TABLE IF EXISTS `ticket_source_provider`;

CREATE TABLE `ticket_source_provider` (
  `provider_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '票源提供方主键',
  `provider_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '稳定编码：LOCAL_MOCK/PIAONIU/DAMAI/MAOYAN',
  `provider_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '票源提供方名称',
  `provider_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MOCK/AGGREGATOR/PRIMARY_MARKET/OTHER',
  `access_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'LOCAL_MOCK/HTTP_API',
  `adapter_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '统一网关中的适配器编码，不保存Java类名',
  `provider_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ENABLED/DISABLED',
  `priority` int(10) unsigned NOT NULL DEFAULT '100' COMMENT '同一资源存在多个票源时的候选优先级，数字越小越优先',
  `base_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方接口基础地址，本地模拟器可空',
  `credential_ref` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '凭证引用，如环境变量名/密钥中心键名，禁止保存密钥明文',
  `connect_timeout_ms` int(10) unsigned NOT NULL DEFAULT '3000' COMMENT '连接超时毫秒',
  `read_timeout_ms` int(10) unsigned NOT NULL DEFAULT '5000' COMMENT '读取超时毫秒',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `version` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '配置并发版本号',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`provider_id`),
  UNIQUE KEY `uk_ticket_source_provider_code` (`provider_code`),
  UNIQUE KEY `uk_ticket_source_adapter_code` (`adapter_code`),
  KEY `idx_ticket_source_provider_status_priority` (`provider_status`,`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源提供方注册表';

/*Data for the table `ticket_source_provider` */

insert  into `ticket_source_provider`(`provider_id`,`provider_code`,`provider_name`,`provider_type`,`access_mode`,`adapter_code`,`provider_status`,`priority`,`base_url`,`credential_ref`,`connect_timeout_ms`,`read_timeout_ms`,`remark`,`version`,`create_time`,`update_time`) values 
(1,'MOCK_DAMAI','大麦模拟接口 Mock Provider','MOCK','LOCAL_MOCK','MOCK_DAMAI','ENABLED',1,'http://localhost:8080/mock-ticket-source/api/v1',NULL,1000,3000,'LOCAL_MOCK：模拟第三方票源接口返回与履约',4,'2026-08-02 07:05:01','2026-08-23 10:47:18');

/*Table structure for table `ticket_source_reconciliation_batch` */

DROP TABLE IF EXISTS `ticket_source_reconciliation_batch`;

CREATE TABLE `ticket_source_reconciliation_batch` (
  `batch_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_id` bigint(20) unsigned NOT NULL,
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `date_from` datetime DEFAULT NULL,
  `date_to` datetime DEFAULT NULL,
  `batch_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PROCESSING/SUCCESS/PARTIAL_FAILED/FAILED',
  `total_count` int(10) unsigned NOT NULL DEFAULT '0',
  `matched_count` int(10) unsigned NOT NULL DEFAULT '0',
  `difference_count` int(10) unsigned NOT NULL DEFAULT '0',
  `error_count` int(10) unsigned NOT NULL DEFAULT '0',
  `start_time` datetime NOT NULL,
  `finish_time` datetime DEFAULT NULL,
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`batch_id`),
  UNIQUE KEY `uk_reconcile_batch_no` (`batch_no`),
  KEY `idx_reconcile_provider_time` (`provider_id`,`start_time`),
  KEY `idx_reconcile_status` (`batch_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源对账批次';

/*Data for the table `ticket_source_reconciliation_batch` */

/*Table structure for table `ticket_source_reconciliation_detail` */

DROP TABLE IF EXISTS `ticket_source_reconciliation_detail`;

CREATE TABLE `ticket_source_reconciliation_detail` (
  `detail_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `batch_id` bigint(20) unsigned NOT NULL,
  `order_id` bigint(20) unsigned NOT NULL,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `compare_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'MATCH/DIFFERENCE/ERROR',
  `difference_types` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `local_order_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_order_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `local_pay_amount` decimal(10,2) DEFAULT NULL,
  `provider_pay_amount` decimal(10,2) DEFAULT NULL,
  `local_refund_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_refund_status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `local_refund_amount` decimal(10,2) DEFAULT NULL,
  `provider_refund_amount` decimal(10,2) DEFAULT NULL,
  `local_valid_ticket_count` int(10) unsigned NOT NULL DEFAULT '0',
  `provider_valid_ticket_count` int(10) unsigned NOT NULL DEFAULT '0',
  `local_ticket_total` int(10) unsigned NOT NULL DEFAULT '0',
  `provider_ticket_total` int(10) unsigned NOT NULL DEFAULT '0',
  `error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `snapshot_text` longtext COLLATE utf8mb4_unicode_ci,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_reconcile_batch_order` (`batch_id`,`order_id`),
  KEY `idx_reconcile_detail_status` (`batch_id`,`compare_status`),
  KEY `idx_reconcile_detail_provider_order` (`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源订单退款凭证对账明细';

/*Data for the table `ticket_source_reconciliation_detail` */

/*Table structure for table `ticket_source_refund_bridge` */

DROP TABLE IF EXISTS `ticket_source_refund_bridge`;

CREATE TABLE `ticket_source_refund_bridge` (
  `bridge_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `refund_id` bigint(20) unsigned NOT NULL,
  `order_id` bigint(20) unsigned NOT NULL,
  `order_bridge_id` bigint(20) unsigned NOT NULL,
  `provider_id` bigint(20) unsigned NOT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `provider_refund_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_refund_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `provider_refund_quote_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bridge_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PENDING_REVIEW/REQUESTING/PROCESSING/RETRY_WAIT/SUCCESS/REJECTED/MANUAL_REVIEW',
  `provider_refund_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_REQUESTED',
  `refund_amount` decimal(10,2) NOT NULL,
  `fee_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `refundable_delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `non_refundable_delivery_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `promotion_rollback_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `currency_code` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `reason_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `request_idempotency_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `retry_count` int(10) unsigned NOT NULL DEFAULT '0',
  `max_retry_count` int(10) unsigned NOT NULL DEFAULT '5',
  `manual_hold` tinyint(1) NOT NULL DEFAULT '0',
  `next_attempt_time` datetime DEFAULT NULL,
  `provider_request_time` datetime DEFAULT NULL,
  `provider_refund_time` datetime DEFAULT NULL,
  `provider_refund_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_operation` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_sync_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_error_retryable` tinyint(1) NOT NULL DEFAULT '0',
  `response_snapshot` longtext COLLATE utf8mb4_unicode_ci,
  `version` int(10) unsigned NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`bridge_id`),
  UNIQUE KEY `uk_source_refund_local` (`refund_id`),
  UNIQUE KEY `uk_source_refund_idempotency` (`request_idempotency_key`),
  UNIQUE KEY `uk_source_refund_provider` (`provider_id`,`provider_refund_id`),
  KEY `idx_source_refund_due` (`manual_hold`,`bridge_status`,`next_attempt_time`),
  KEY `idx_source_refund_order` (`order_id`),
  KEY `idx_source_refund_provider_order` (`provider_id`,`provider_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地退款与第三方退款桥接';

/*Data for the table `ticket_source_refund_bridge` */

/*Table structure for table `ticket_source_session_mapping` */

DROP TABLE IF EXISTS `ticket_source_session_mapping`;

CREATE TABLE `ticket_source_session_mapping` (
  `mapping_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '场次映射主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `project_mapping_id` bigint(20) unsigned NOT NULL COMMENT '所属第三方项目映射ID',
  `session_id` bigint(20) unsigned NOT NULL COMMENT '本地标准场次ID',
  `provider_session_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方场次ID',
  `provider_session_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方场次名称快照',
  `mapping_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/BOUND/DISABLED/INVALID',
  `source_sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT '统一后的第三方销售状态',
  `source_status_value` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态码或状态值',
  `source_status_text` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态说明',
  `source_data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本、ETag或更新时间戳',
  `source_updated_time` datetime DEFAULT NULL COMMENT '第三方资源更新时间',
  `last_sync_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEVER' COMMENT 'NEVER/SUCCESS/FAILED',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误码',
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误摘要',
  `source_payload_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '最近一次第三方原始响应快照',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`mapping_id`),
  UNIQUE KEY `uk_source_session_provider_local` (`provider_id`,`session_id`),
  UNIQUE KEY `uk_source_session_provider_remote` (`provider_id`,`project_mapping_id`,`provider_session_id`),
  KEY `idx_source_session_project_mapping` (`project_mapping_id`),
  KEY `idx_source_session_local` (`session_id`),
  KEY `idx_source_session_status_sync` (`mapping_status`,`last_sync_status`,`last_sync_time`)
) ENGINE=InnoDB AUTO_INCREMENT=710054 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地场次与第三方场次映射';

/*Data for the table `ticket_source_session_mapping` */

insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(1,1,1,12015,'MOCK-SESSION-11001','Provider 北京站·9月12日 19:30','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','DEMO-SESSION-20260825095630','2026-08-25 09:56:30','SUCCESS','2026-08-25 09:57:58',NULL,NULL,NULL,'2026-08-03 00:47:29','2026-08-25 09:57:58');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(4,1,3,190003,'MOCK-SESSION-12001','北京站·9月5日 19:30','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','MOCK-SESSION-12001-demo-v1','2026-08-10 10:47:00','SUCCESS','2026-08-12 10:01:38',NULL,NULL,NULL,'2026-08-10 10:50:11','2026-08-12 10:01:38');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710001,1,700001,400001,'DEMO-MOCK-SESSION-001','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-001-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710002,1,700002,400002,'DEMO-MOCK-SESSION-002','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-002-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710003,1,700003,400003,'DEMO-MOCK-SESSION-003','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-003-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710004,1,700004,400004,'DEMO-MOCK-SESSION-004','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-004-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710005,1,700005,400005,'DEMO-MOCK-SESSION-005','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-005-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710006,1,700006,400006,'DEMO-MOCK-SESSION-006','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-006-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710007,1,700007,400007,'DEMO-MOCK-SESSION-007','北京站·演示场','BOUND','PRESALE','DEMO_PRESALE','模拟预售','DEMO-MOCK-SESSION-007-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710008,1,700008,400008,'DEMO-MOCK-SESSION-008','北京站·演示场','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','DEMO-MOCK-SESSION-008-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710039,1,700039,490101,'DEMO-AI-S-490101','上海站·9月2日场','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-S-490101-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710040,1,700040,490102,'DEMO-AI-S-490102','上海站·9月5日场','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-S-490102-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710041,1,700041,490103,'DEMO-AI-S-490103','上海站·9月18日场','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-S-490103-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_session_mapping`(`mapping_id`,`provider_id`,`project_mapping_id`,`session_id`,`provider_session_id`,`provider_session_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(710042,1,700042,490104,'DEMO-AI-S-490104','广州站·9月3日场','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','DEMO-AI-S-490104-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');

/*Table structure for table `ticket_source_settlement_detail` */

DROP TABLE IF EXISTS `ticket_source_settlement_detail`;

CREATE TABLE `ticket_source_settlement_detail` (
  `detail_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '结算明细主键',
  `period_id` bigint(20) unsigned NOT NULL COMMENT '所属账期',
  `detail_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SALE/REFUND/ADJUSTMENT',
  `source_key` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SALE:orderId / REFUND:refundId / ADJ:uuid',
  `order_id` bigint(20) unsigned DEFAULT NULL,
  `refund_id` bigint(20) unsigned DEFAULT NULL,
  `provider_order_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference_no` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本地订单号/退款号/外部参考号',
  `business_time` datetime NOT NULL COMMENT '销售支付时间/退款完成时间/调整时间',
  `user_amount` decimal(14,2) DEFAULT NULL COMMENT '用户实付/用户退款金额，仅用于核对，不作为Provider结算金额',
  `provider_settlement_amount` decimal(14,2) DEFAULT NULL COMMENT 'Provider结算基准金额',
  `amount_effect` decimal(14,2) NOT NULL COMMENT '对本期应付Provider的影响：销售正、退款负、调整可正负',
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_ts_settlement_source` (`period_id`,`source_key`),
  KEY `idx_ts_settlement_detail_order` (`order_id`),
  KEY `idx_ts_settlement_detail_refund` (`refund_id`),
  KEY `idx_ts_settlement_detail_type` (`period_id`,`detail_type`,`business_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源账期结算明细';

/*Data for the table `ticket_source_settlement_detail` */

/*Table structure for table `ticket_source_settlement_period` */

DROP TABLE IF EXISTS `ticket_source_settlement_period`;

CREATE TABLE `ticket_source_settlement_period` (
  `period_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '账期结算主键',
  `settlement_no` varchar(96) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '结算单号',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT 'Provider ID',
  `provider_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Provider稳定编码快照',
  `date_from` date NOT NULL COMMENT '账期开始日期（含）',
  `date_to` date NOT NULL COMMENT '账期结束日期（含）',
  `period_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/CONFIRMED/PAID/CARRIED_FORWARD',
  `sale_order_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '本期销售订单数',
  `refund_order_count` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '本期退款冲减数',
  `sale_settlement_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '本期销售应付Provider金额',
  `refund_deduction_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '本期退款冲减Provider金额（正数展示）',
  `adjustment_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '人工/跨期调整净额，可正可负',
  `net_payable_amount` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '本期净应付Provider=销售-退款+调整',
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY',
  `confirmed_time` datetime DEFAULT NULL COMMENT '账单确认时间',
  `close_time` datetime DEFAULT NULL COMMENT '付款完成或结转完成时间',
  `close_mode` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'PAID/CARRIED_FORWARD',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`period_id`),
  UNIQUE KEY `uk_ts_settlement_no` (`settlement_no`),
  UNIQUE KEY `uk_ts_settlement_provider_range` (`provider_id`,`date_from`,`date_to`),
  KEY `idx_ts_settlement_provider_status` (`provider_id`,`period_status`,`date_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方票源账期结算单';

/*Data for the table `ticket_source_settlement_period` */

/*Table structure for table `ticket_source_shipment` */

DROP TABLE IF EXISTS `ticket_source_shipment`;

CREATE TABLE `ticket_source_shipment` (
  `shipment_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '物流记录主键',
  `order_bridge_id` bigint(20) unsigned NOT NULL COMMENT '第三方订单桥接ID',
  `provider_shipment_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方物流单ID',
  `shipment_status` varchar(24) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAIT_SHIPMENT' COMMENT 'NOT_REQUIRED/WAIT_SHIPMENT/SHIPPED/IN_TRANSIT/DELIVERED/EXCEPTION/RETURNED',
  `carrier_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '快递公司编码',
  `carrier_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '快递公司',
  `waybill_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '运单号',
  `tracking_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方物流查询地址',
  `shipped_time` datetime DEFAULT NULL COMMENT '发货时间',
  `signed_time` datetime DEFAULT NULL COMMENT '签收时间',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `provider_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方物流版本',
  `last_sync_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误码',
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`shipment_id`),
  UNIQUE KEY `uk_source_shipment_order` (`order_bridge_id`),
  KEY `idx_source_shipment_status` (`shipment_status`,`update_time`),
  KEY `idx_source_shipment_waybill` (`waybill_no`),
  KEY `idx_source_shipment_status_sync` (`shipment_status`,`last_sync_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方纸质票物流';

/*Data for the table `ticket_source_shipment` */

/*Table structure for table `ticket_source_sku_mapping` */

DROP TABLE IF EXISTS `ticket_source_sku_mapping`;

CREATE TABLE `ticket_source_sku_mapping` (
  `mapping_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '票档映射主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `session_mapping_id` bigint(20) unsigned NOT NULL COMMENT '所属第三方场次映射ID',
  `sku_id` bigint(20) unsigned NOT NULL COMMENT '本地标准票档ID',
  `provider_sku_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方票品/票档ID',
  `provider_sku_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方票品名称快照',
  `mapping_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/BOUND/DISABLED/INVALID',
  `source_sale_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT '统一后的第三方销售状态',
  `source_status_value` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态码或状态值',
  `source_status_text` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方原始状态说明',
  `inventory_mode` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNKNOWN' COMMENT 'UNKNOWN/SNAPSHOT/REALTIME_QUERY',
  `available_stock_snapshot` int(10) unsigned DEFAULT NULL COMMENT '第三方可售库存快照；NULL表示未知，不等于0',
  `face_price` decimal(10,2) DEFAULT NULL COMMENT '第三方票面价快照',
  `sale_price` decimal(10,2) DEFAULT NULL COMMENT '面向用户销售价快照',
  `settlement_price` decimal(10,2) DEFAULT NULL COMMENT '与第三方结算价快照',
  `currency_code` char(3) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT 'ISO 4217币种编码',
  `source_data_version` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方数据版本、ETag或更新时间戳',
  `source_updated_time` datetime DEFAULT NULL COMMENT '第三方资源更新时间',
  `last_sync_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEVER' COMMENT 'NEVER/SUCCESS/FAILED',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `last_inventory_sync_time` datetime DEFAULT NULL COMMENT '最近一次库存同步尝试成功时间',
  `last_error_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误码',
  `last_error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最近同步错误摘要',
  `source_payload_snapshot` longtext COLLATE utf8mb4_unicode_ci COMMENT '最近一次第三方原始响应快照',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`mapping_id`),
  UNIQUE KEY `uk_source_sku_provider_local` (`provider_id`,`sku_id`),
  UNIQUE KEY `uk_source_sku_provider_remote` (`provider_id`,`session_mapping_id`,`provider_sku_id`),
  KEY `idx_source_sku_session_mapping` (`session_mapping_id`),
  KEY `idx_source_sku_local` (`sku_id`),
  KEY `idx_source_sku_sale_stock` (`mapping_status`,`source_sale_status`,`available_stock_snapshot`),
  KEY `idx_source_sku_sync` (`last_sync_status`,`last_sync_time`)
) ENGINE=InnoDB AUTO_INCREMENT=720108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='本地票档与第三方票品映射及库存快照';

/*Data for the table `ticket_source_sku_mapping` */

insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(1,1,1,22024,'MOCK-SKU-21001','Provider 普通票 366','BOUND','ON_SALE','ON_SALE','模拟在售','REALTIME_QUERY',65,380.00,366.00,311.00,'CNY','DEMO-SKU-20260825095630','2026-08-25 09:56:30','SUCCESS','2026-08-25 09:57:58','2026-09-02 21:14:20',NULL,NULL,NULL,'2026-08-03 00:47:29','2026-09-02 21:14:20');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(2,1,1,22025,'MOCK-SKU-21002','单日 VIP 880','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','REALTIME_QUERY',32,880.00,880.00,820.00,'CNY','MOCK-SKU-21002-demo-v1','2026-08-14 23:13:55','SUCCESS','2026-08-25 09:57:58','2026-09-02 21:14:20',NULL,NULL,'{\"availableStock\":32,\"currencyCode\":\"CNY\",\"dataVersion\":\"MOCK-SKU-21002-demo-v1\",\"facePrice\":880.00,\"inventoryMode\":\"REALTIME_QUERY\",\"providerSessionId\":\"MOCK-SESSION-11001\",\"providerSkuId\":\"MOCK-SKU-21002\",\"salePrice\":880.00,\"saleStatus\":\"ON_SALE\",\"settlementPrice\":820.00,\"skuName\":\"单日 VIP 880\",\"updateTime\":\"2026-08-14T23:13:55\"}','2026-08-03 00:47:29','2026-09-02 21:14:20');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(7,1,1,22030,'MOCK-SKU-21007','纪念纸质票 1280','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','REALTIME_QUERY',22,1280.00,1280.00,1180.00,'CNY','MOCK-SKU-21007-demo-v1','2026-08-12 22:51:37','SUCCESS','2026-08-25 09:57:58','2026-09-02 21:14:20',NULL,NULL,'{\"availableStock\":22,\"currencyCode\":\"CNY\",\"dataVersion\":\"MOCK-SKU-21007-demo-v1\",\"facePrice\":1280.00,\"inventoryMode\":\"REALTIME_QUERY\",\"providerSessionId\":\"MOCK-SESSION-11001\",\"providerSkuId\":\"MOCK-SKU-21007\",\"salePrice\":1280.00,\"saleStatus\":\"ON_SALE\",\"settlementPrice\":1180.00,\"skuName\":\"纪念纸质票 1280\",\"updateTime\":\"2026-08-12T22:51:37\"}','2026-08-06 22:26:07','2026-09-02 21:14:20');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(8,1,4,290003,'MOCK-SKU-22001','一楼普通席 380（纸质票）','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售','REALTIME_QUERY',26,380.00,380.00,350.00,'CNY','MOCK-SKU-22001-demo-v1','2026-08-11 00:28:52','SUCCESS','2026-08-12 10:01:38','2026-09-03 00:37:01',NULL,NULL,'{\"availableStock\":27,\"currencyCode\":\"CNY\",\"dataVersion\":\"MOCK-SKU-22001-demo-v1\",\"facePrice\":380.00,\"inventoryMode\":\"REALTIME_QUERY\",\"providerSessionId\":\"MOCK-SESSION-12001\",\"providerSkuId\":\"MOCK-SKU-22001\",\"salePrice\":380.00,\"saleStatus\":\"ON_SALE\",\"settlementPrice\":350.00,\"skuName\":\"一楼普通席 380（纸质票）\",\"updateTime\":\"2026-08-11T00:28:52\"}','2026-08-10 10:50:11','2026-09-03 00:37:01');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(9,1,4,290004,'MOCK-SKU-22002','一楼前排席 680（纸质票·未知库存）','BOUND','ON_SALE','MOCK_ON_SALE','模拟在售·库存待实时确认','SNAPSHOT',NULL,680.00,680.00,640.00,'CNY','MOCK-SKU-22002-demo-v1','2026-08-10 10:47:00','SUCCESS','2026-08-12 10:01:38','2026-09-03 00:36:56',NULL,NULL,NULL,'2026-08-10 10:50:11','2026-09-03 00:36:56');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720001,1,710001,500001,'DEMO-MOCK-SKU-001-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',49,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-001-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-18 01:14:04',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-18 01:14:03');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720002,1,710001,500002,'DEMO-MOCK-SKU-001-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',19,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-001-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-18 01:14:03',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-18 01:14:03');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720003,1,710002,500003,'DEMO-MOCK-SKU-002-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',50,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-002-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720004,1,710002,500004,'DEMO-MOCK-SKU-002-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',20,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-002-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720005,1,710003,500005,'DEMO-MOCK-SKU-003-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',51,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-003-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720006,1,710003,500006,'DEMO-MOCK-SKU-003-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',21,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-003-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720007,1,710004,500007,'DEMO-MOCK-SKU-004-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',52,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-004-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720008,1,710004,500008,'DEMO-MOCK-SKU-004-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',22,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-004-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720009,1,710005,500009,'DEMO-MOCK-SKU-005-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',53,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-005-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720010,1,710005,500010,'DEMO-MOCK-SKU-005-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',23,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-005-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720011,1,710006,500011,'DEMO-MOCK-SKU-006-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',54,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-006-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720012,1,710006,500012,'DEMO-MOCK-SKU-006-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',24,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-006-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720013,1,710007,500013,'DEMO-MOCK-SKU-007-A','普通票 280','BOUND','PRESALE','DEMO_PRESALE','模拟预售','REALTIME_QUERY',55,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-007-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720014,1,710007,500014,'DEMO-MOCK-SKU-007-B','优享票 1280','BOUND','PRESALE','DEMO_PRESALE','模拟预售','REALTIME_QUERY',18,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-007-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-17 13:22:32',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720015,1,710008,500015,'DEMO-MOCK-SKU-008-A','普通票 280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',54,280.00,280.00,257.60,'CNY','DEMO-MOCK-SKU-008-A-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-26 05:23:09',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-26 05:23:09');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720016,1,710008,500016,'DEMO-MOCK-SKU-008-B','优享票 1280','BOUND','ON_SALE','DEMO_ON_SALE','模拟在售','REALTIME_QUERY',19,1280.00,1280.00,1177.60,'CNY','DEMO-MOCK-SKU-008-B-v1','2026-08-17 13:22:32','SUCCESS','2026-08-17 13:22:32','2026-08-26 05:23:06',NULL,NULL,NULL,'2026-08-17 13:22:32','2026-08-26 05:23:05');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720077,1,710039,590201,'DEMO-AI-K-590201','普通票 280','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',60,280.00,280.00,280.00,'CNY','DEMO-AI-K-590201-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 12:45:18',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 12:45:18');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720078,1,710039,590202,'DEMO-AI-K-590202','VIP票 1280','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',20,1280.00,1280.00,1280.00,'CNY','DEMO-AI-K-590202-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 12:45:18',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 12:45:18');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720079,1,710040,590203,'DEMO-AI-K-590203','普通票 180','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',70,180.00,180.00,180.00,'CNY','DEMO-AI-K-590203-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720080,1,710040,590204,'DEMO-AI-K-590204','优享票 680','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',25,680.00,680.00,680.00,'CNY','DEMO-AI-K-590204-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720081,1,710041,590205,'DEMO-AI-K-590205','普通票 380','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',50,380.00,380.00,380.00,'CNY','DEMO-AI-K-590205-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-09-02 23:19:03',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-09-02 23:19:02');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720082,1,710041,590206,'DEMO-AI-K-590206','VIP票 1580','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',18,1580.00,1580.00,1580.00,'CNY','DEMO-AI-K-590206-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-09-02 23:19:03',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-09-02 23:19:02');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720083,1,710042,590207,'DEMO-AI-K-590207','普通票 280','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',65,280.00,280.00,280.00,'CNY','DEMO-AI-K-590207-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_sku_mapping`(`mapping_id`,`provider_id`,`session_mapping_id`,`sku_id`,`provider_sku_id`,`provider_sku_name`,`mapping_status`,`source_sale_status`,`source_status_value`,`source_status_text`,`inventory_mode`,`available_stock_snapshot`,`face_price`,`sale_price`,`settlement_price`,`currency_code`,`source_data_version`,`source_updated_time`,`last_sync_status`,`last_sync_time`,`last_inventory_sync_time`,`last_error_code`,`last_error_message`,`source_payload_snapshot`,`create_time`,`update_time`) values 
(720084,1,710042,590208,'DEMO-AI-K-590208','VIP票 1280','BOUND','ON_SALE','DEMO_AI_ON_SALE','麦麦城市演示模拟票源','SNAPSHOT',21,1280.00,1280.00,1280.00,'CNY','DEMO-AI-K-590208-v1','2026-08-31 11:54:19','SUCCESS','2026-08-31 11:54:19','2026-08-31 11:54:19',NULL,NULL,NULL,'2026-08-31 11:54:19','2026-08-31 11:54:19');

/*Table structure for table `ticket_source_submit_idempotency` */

DROP TABLE IF EXISTS `ticket_source_submit_idempotency`;

CREATE TABLE `ticket_source_submit_idempotency` (
  `idempotency_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '提交订单幂等主键',
  `client_submit_no` varchar(96) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端提交流水号，全局唯一',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `request_fingerprint` char(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提交内容SHA-256指纹',
  `order_id` bigint(20) unsigned DEFAULT NULL COMMENT '成功创建后的本地订单ID',
  `submit_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'PROCESSING/UNKNOWN_RESULT/SUCCESS/FAILED/MANUAL_REVIEW',
  `error_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败编码',
  `error_message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`idempotency_id`),
  UNIQUE KEY `uk_v13_submit_no` (`client_submit_no`),
  KEY `idx_v13_submit_user_time` (`user_id`,`create_time`),
  KEY `idx_v13_submit_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V1.3用户侧提交订单幂等表';

/*Data for the table `ticket_source_submit_idempotency` */

/*Table structure for table `ticket_source_venue_mapping` */

DROP TABLE IF EXISTS `ticket_source_venue_mapping`;

CREATE TABLE `ticket_source_venue_mapping` (
  `mapping_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '场馆映射主键',
  `provider_id` bigint(20) unsigned NOT NULL COMMENT '票源提供方ID',
  `venue_id` bigint(20) unsigned NOT NULL COMMENT '本地场馆ID',
  `provider_venue_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方场馆ID',
  `mapping_status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'BOUND' COMMENT 'BOUND/DISABLED',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最近同步时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`mapping_id`),
  UNIQUE KEY `uk_source_venue_remote` (`provider_id`,`provider_venue_id`),
  UNIQUE KEY `uk_source_venue_local` (`provider_id`,`venue_id`),
  KEY `idx_source_venue_status` (`provider_id`,`mapping_status`)
) ENGINE=InnoDB AUTO_INCREMENT=730014 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方场馆映射';

/*Data for the table `ticket_source_venue_mapping` */

insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(1,1,9,'MOCK-VENUE-BJ-001','BOUND','2026-08-25 09:57:58','2026-08-06 22:26:07','2026-08-25 09:57:58');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(22,1,11,'MOCK-VENUE-BJ-002','BOUND','2026-08-12 10:01:38','2026-08-10 10:50:11','2026-08-12 10:01:38');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730001,1,600001,'DEMO-MOCK-VENUE-001','BOUND','2026-08-23 12:55:44','2026-08-17 13:22:32','2026-08-23 12:55:44');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730002,1,600002,'DEMO-MOCK-VENUE-002','BOUND','2026-08-17 13:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730003,1,600003,'DEMO-MOCK-VENUE-003','BOUND','2026-08-17 13:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730004,1,600004,'DEMO-MOCK-VENUE-004','BOUND','2026-08-24 18:58:06','2026-08-17 13:22:32','2026-08-24 18:58:06');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730005,1,600005,'DEMO-MOCK-VENUE-005','BOUND','2026-08-17 13:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730006,1,600006,'DEMO-MOCK-VENUE-006','BOUND','2026-08-17 13:22:32','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730007,1,690101,'DEMO-AI-V-690101','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730008,1,690102,'DEMO-AI-V-690102','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730009,1,690103,'DEMO-AI-V-690103','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730010,1,690104,'DEMO-AI-V-690104','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730011,1,690105,'DEMO-AI-V-690105','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730012,1,690106,'DEMO-AI-V-690106','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');
insert  into `ticket_source_venue_mapping`(`mapping_id`,`provider_id`,`venue_id`,`provider_venue_id`,`mapping_status`,`last_sync_time`,`create_time`,`update_time`) values 
(730013,1,690107,'DEMO-AI-V-690107','BOUND','2026-08-31 11:54:19','2026-08-31 11:54:19','2026-08-31 11:54:19');

/*Table structure for table `user_account` */

DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户主键',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号，登录账号，唯一',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像，可空',
  `account_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号状态',
  `create_time` datetime NOT NULL COMMENT '创建时间/注册时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_account_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号';

/*Data for the table `user_account` */

/*Table structure for table `user_address` */

DROP TABLE IF EXISTS `user_address`;

CREATE TABLE `user_address` (
  `address_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '地址主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '所属用户ID',
  `receiver_name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `province` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '市',
  `district` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区/县',
  `country_code` varchar(8) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家/地区编码，如CN',
  `province_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省级行政区编码',
  `city_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市行政区编码',
  `area_code` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县行政区编码',
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址/门牌号',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否默认地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_address_default` (`user_id`,`is_default`),
  KEY `idx_user_address_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址';

/*Data for the table `user_address` */

/*Table structure for table `venue` */

DROP TABLE IF EXISTS `venue`;

CREATE TABLE `venue` (
  `venue_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '场馆主键',
  `venue_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场馆名称',
  `city_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属城市',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '场馆地址',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度，可空',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度，可空',
  `coordinate_system` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GCJ02' COMMENT '坐标系：GCJ02/WGS84/BD09/UNKNOWN',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`venue_id`),
  UNIQUE KEY `uk_venue_city_name_address` (`city_name`,`venue_name`,`address`),
  KEY `idx_venue_city` (`city_name`),
  KEY `idx_venue_name` (`venue_name`)
) ENGINE=InnoDB AUTO_INCREMENT=690108 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆';

/*Data for the table `venue` */

insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(9,'国家体育场','北京','北京市朝阳区国家体育场',116.3974770,39.9928650,'GCJ02','2026-08-03 00:47:28','2026-08-25 09:57:58');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(11,'北京保利剧院','北京','北京市东城区东直门南大街14号',116.4381000,39.9335000,'GCJ02','2026-08-10 10:50:11','2026-08-12 10:01:38');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600001,'凯迪拉克中心','北京','北京市海淀区复兴路69号',116.2741000,39.9108000,'GCJ02','2026-08-17 13:22:32','2026-08-23 12:55:44');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600002,'国家大剧院','北京','北京市西城区西长安街2号',116.3830000,39.9033000,'GCJ02','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600003,'北京展览馆剧场','北京','北京市西城区西直门外大街135号',116.3440000,39.9400000,'GCJ02','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600004,'北京天桥艺术中心','北京','北京市西城区天桥南大街9号',116.3980000,39.8830000,'GCJ02','2026-08-17 13:22:32','2026-08-24 18:58:06');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600005,'国家体育馆','北京','北京市朝阳区天辰东路9号',116.3870000,39.9990000,'GCJ02','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(600006,'北京喜剧院','北京','北京市东城区朝阳门北大街11号',116.4300000,39.9280000,'GCJ02','2026-08-17 13:22:32','2026-08-17 13:22:32');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690101,'梅赛德斯-奔驰文化中心','上海','上海市浦东新区世博大道1200号',121.4930000,31.1850000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690102,'上海文化广场','上海','上海市黄浦区复兴中路597号',121.4660000,31.2120000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690103,'广州体育馆','广州','广州市白云区白云大道南783号',113.2800000,23.1830000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690104,'广州大剧院','广州','广州市天河区珠江西路1号',113.3240000,23.1150000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690105,'深圳湾体育中心','深圳','深圳市南山区滨海大道3001号',113.9510000,22.5180000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690106,'深圳保利剧院','深圳','深圳市南山区后海滨路3013号',113.9410000,22.5180000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');
insert  into `venue`(`venue_id`,`venue_name`,`city_name`,`address`,`longitude`,`latitude`,`coordinate_system`,`create_time`,`update_time`) values 
(690107,'成都金融城演艺中心','成都','成都市高新区天府大道北段',104.0690000,30.5790000,'GCJ02','2026-08-30 17:28:13','2026-08-30 17:28:13');

/*Table structure for table `want_record` */

DROP TABLE IF EXISTS `want_record`;

CREATE TABLE `want_record` (
  `want_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '想看记录主键',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `project_id` bigint(20) unsigned NOT NULL COMMENT '演出项目ID',
  `want_time` datetime NOT NULL COMMENT '想看时间',
  PRIMARY KEY (`want_id`),
  UNIQUE KEY `uk_want_user_project` (`user_id`,`project_id`),
  KEY `idx_want_user_time` (`user_id`,`want_time`),
  KEY `idx_want_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='想看记录';

/*Data for the table `want_record` */

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
