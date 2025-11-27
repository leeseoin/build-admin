CREATE TABLE `tb_category` (
	`id`	INT UNSIGNED	NOT NULL,
	`id2`	INT UNSIGNED	NOT NULL,
	`name`	VARCHAR	NOT NULL,
	`releaseYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`createdBy`	INT UNSIGNED	NOT NULL,
	`updatedAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`updatedBy`	INT UNSIGNED	NOT NULL,
	`deletedAt`	DATETIME	NULL,
	`deletedBy`	INT UNSIGNED	NULL,
	`delYn`	BOOLEAN	NOT NULL
);

CREATE TABLE `tb_file` (
	`id`	BIGINT UNSIGNED	NOT NULL,
	`path`	VARCHAR	NOT NULL,
	`createdBy`	BIGINT UNSIGNED	NOT NULL,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`delYn`	BOOLEAN	NOT NULL	DEFAULT 'N',
	`deletedBy`	BIGINT UNSIGNED	NULL,
	`deletedAt`	DATETIME	NULL,
	`isAdminCreated`	BOOLEAN	NOT NULL
);

CREATE TABLE `tb_interest` (
	`artistId`	INT	NOT NULL,
	`userId`	INT	NOT NULL
);

CREATE TABLE `tb_user` (
	`userId`	BIGINT UNSIGNED	NOT NULL,
	`loginId`	VARCHAR	NOT NULL,
	`userName`	VARCHAR	NOT NULL,
	`socialLoginYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`lastLoginAt`	DATETIME	NULL	DEFAULT NULL,
	`subscribeInService`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`registeredPath`	ENUM('EMAIL', 'SOCIAL')	NOT NULL	DEFAULT 0101	COMMENT ''0101' : EMAIL',
	`withdrawalType`	ENUM('SELF', 'SYSTEM')	NULL	COMMENT 'SELF: 회원탈퇴, SYSTEM: 장기간 휴면 탈퇴',
	`password`	VARCHAR	NOT NULL,
	`registeredAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`updatedBy`	BIGINT UNSIGNED	NULL,
	`updatedAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`deactivatedAt`	DATETIME	NULL,
	`withdrawalAt`	DATETIME	NULL,
	`language`	VARCHAR(10)	NOT NULL	DEFAULT 'ENG'
);

CREATE TABLE `tb_sttCommand` (
	`id`	INT	NOT NULL,
	`artistId`	INT	NOT NULL,
	`command`	VARCHAR	NULL
);

CREATE TABLE `tb_artist` (
	`id`	INT UNSIGNED	NOT NULL,
	`name`	VARCHAR	NOT NULL,
	`thumbnail`	BIGINT UNSIGNED	NULL	COMMENT 'tb_files',
	`favoriteImage`	BIGINT UNSIGNED	NULL	COMMENT 'tb_files',
	`createdAt`	DATETIME	NOT NULL,
	`createdBy`	INT UNSIGNED	NOT NULL,
	`updatedAt`	DATETIME	NOT NULL,
	`updatedBy`	INT UNSIGNED	NOT NULL,
	`releaseYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`delYn`	BOOLEAN	NOT NULL	DEFAULT FALSE
);

CREATE TABLE `tb_user_device` (
	`id`	BIGINT UNSIGNED	NOT NULL,
	`userId`	BIGINT UNSIGNED	NOT NULL,
	`id2`	INT UNSIGNED	NOT NULL,
	`imageInOff`	BIGINT UNSIGNED	NULL,
	`imageInOn`	BIGINT UNSIGNED	NULL,
	`backImageInOn`	BIGINT UNSIGNED	NULL,
	`imageMainOn`	BIGINT UNSIGNED	NULL,
	`backImageMainOn`	BIGINT UNSIGNED	NULL,
	`image`	BIGINT UNSIGNED	NULL,
	`backImage`	BIGINT UNSIGNED	NULL,
	`serialNumber`	VARCHAR	NOT NULL,
	`releaseYn`	BOOLEAN	NOT NULL,
	`macAddress`	VARCHAR	NOT NULL,
	`bluetoothGroupName`	VARCHAR	NOT NULL
);

CREATE TABLE `.tb_subscription_promotion` (
	`Key`	INT UNSIGNED	NOT NULL,
	`id`	INT UNSIGNED	NOT NULL,
	`discountRate`	INT UNSIGNED	NULL	DEFAULT 0,
	`finalPrice`	INT UNSIGNED	NULL	DEFAULT 0,
	`useYn`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE `tb_subscription_hist` (
	`id`	BIGINT UNSIGNED	NOT NULL,
	`userId`	BIGINT UNSIGNED	NOT NULL,
	`subscriptionId`	INT UNSIGNED	NOT NULL,
	`periodMonth`	TINYINT UNSIGNED	NOT NULL	DEFAULT 0,
	`paidAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`price`	DECIMAL(8,2) UNSIGNED	NOT NULL	DEFAULT 0,
	`startAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`endAt`	DATETIME	NOT NULL
);

CREATE TABLE `Untitled` (
	`id`	BIGINT	NOT NULL,
	`mainCategoryCode`	VARCHAR	NOT NULL,
	`mainCategoryName`	VARCHAR	NOT NULL,
	`subCategoryCode`	VARCHAR	NULL,
	`subCategoryName`	VARCHAR	NULL
);

CREATE TABLE `tb_app_version` (
	`id`	INT UNSIGNED	NOT NULL,
	`version`	VARCHAR	NOT NULL,
	`useYn`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE `tb_terms` (
	`id`	INT UNSIGNED	NOT NULL,
	`type`	VARCHAR	NOT NULL	COMMENT 'SERVICE, PRIVACY, LICENSE',
	`version`	VARCHAR	NOT NULL,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`content`	TEXT	NULL,
	`language`	VARCHAR	NOT NULL	DEFAULT KOR,
	`delYn`	BOOLEAN	NOT NULL	DEFAULT FALSE
);

CREATE TABLE `tb_adminAccount` (
	`adminId`	INT UNSIGNED	NOT NULL,
	`loginId`	VARCHAR	NOT NULL,
	`password`	VARCHAR	NOT NULL,
	`name`	VARCHAR	NOT NULL,
	`memo`	VARCHAR	NULL	DEFAULT NULL,
	`level`	VARCHAR(20)	NOT NULL	DEFAULT ADMIN,
	`profileImage`	BIGINT UNSIGNED	NULL,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`createdBy`	INT UNSIGNED	NULL,
	`deletedAt`	DATETIME	NULL,
	`deletedBy`	INT UNSIGNED	NULL
);

CREATE TABLE `tb_refresh_token` (
	`id`	BIGINT UNSIGNED	NOT NULL,
	`token`	VARCHAR(500)	NOT NULL,
	`userId`	BIGINT UNSIGNED	NULL,
	`adminId`	INT UNSIGNED	NULL,
	`userType`	ENUM('USER', 'ADMIN')	NOT NULL	COMMENT 'USER: 일반 사용자, ADMIN: 관리자',
	`expiresAt`	DATETIME	NOT NULL,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`lastUsedAt`	DATETIME	NULL	COMMENT '마지막 사용 시간'
);

CREATE TABLE `tb_magazine` (
	`id`	INT UNSIGNED	NOT NULL,
	`artistId`	INT UNSIGNED	NOT NULL
);

CREATE TABLE `tb_subscription` (
	`id`	INT UNSIGNED	NOT NULL,
	`artistId`	INT UNSIGNED	NOT NULL,
	`name`	VARCHAR	NULL,
	`price`	DECIMAL(8,2) UNSIGNED	NOT NULL	DEFAULT 0,
	`periodMonth`	TINYINT UNSIGNED	NOT NULL	DEFAULT 0,
	`currency`	VARCHAR	NOT NULL	DEFAULT USD,
	`useYn`	BOOLEAN	NOT NULL	DEFAULT FALSE
);

CREATE TABLE `.tb_device` (
	`id`	INT UNSIGNED	NOT NULL,
	`artistId`	INT UNSIGNED	NOT NULL,
	`name`	VARCHAR	NULL,
	`useYn`	BOOLEAN	NOT NULL	DEFAULT TRUE
);

CREATE TABLE `tb_media` (
	`id`	BIGINT UNSIGNED	NOT NULL,
	`categoryId`	INT UNSIGNED	NOT NULL,
	`name`	VARCHAR	NOT NULL,
	`thumbnail`	BIGINT UNSIGNED	NOT NULL	COMMENT 'tb_files',
	`image`	BIGINT UNSIGNED	NOT NULL	COMMENT 'tb_files',
	`releaseYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`submitYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`createdAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`createdBy`	INT UNSIGNED	NOT NULL,
	`updatedAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`updatedBy`	INT UNSIGNED	NOT NULL,
	`deletedAt`	DATETIME	NULL	DEFAULT NULL,
	`deletedBy`	INT UNSIGNED	NULL	DEFAULT NULL,
	`delYn`	BOOLEAN	NOT NULL	DEFAULT FALSE
);

CREATE TABLE `tb_Alert` (
	`id`	INT	NOT NULL,
	`language`	VARCHAR	NOT NULL	DEFAULT KOR,
	`title`	VARCHAR	NULL,
	`content`	VARCHAR	NULL,
	`sendYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`sendAt`	DATETIME	NOT NULL	DEFAULT NOW(),
	`delYn`	BOOLEAN	NOT NULL	DEFAULT FALSE,
	`silentYn`	BOOLEAN	NOT NULL	DEFAULT FALSE
);

CREATE TABLE `Untitled2` (
	`seoinId`	int	NOT NULL,
	`name`	str	NULL,
	`Field2`	VARCHAR(255)	NULL
);

ALTER TABLE `tb_category` ADD CONSTRAINT `PK_TB_CATEGORY` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_file` ADD CONSTRAINT `PK_TB_FILE` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_interest` ADD CONSTRAINT `PK_TB_INTEREST` PRIMARY KEY (
	`artistId`,
	`userId`
);

ALTER TABLE `tb_user` ADD CONSTRAINT `PK_TB_USER` PRIMARY KEY (
	`userId`
);

ALTER TABLE `tb_sttCommand` ADD CONSTRAINT `PK_TB_STTCOMMAND` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_artist` ADD CONSTRAINT `PK_TB_ARTIST` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_user_device` ADD CONSTRAINT `PK_TB_USER_DEVICE` PRIMARY KEY (
	`id`
);

ALTER TABLE `.tb_subscription_promotion` ADD CONSTRAINT `PK_.TB_SUBSCRIPTION_PROMOTION` PRIMARY KEY (
	`Key`
);

ALTER TABLE `tb_subscription_hist` ADD CONSTRAINT `PK_TB_SUBSCRIPTION_HIST` PRIMARY KEY (
	`id`,
	`userId`,
	`subscriptionId`
);

ALTER TABLE `Untitled` ADD CONSTRAINT `PK_UNTITLED` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_app_version` ADD CONSTRAINT `PK_TB_APP_VERSION` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_terms` ADD CONSTRAINT `PK_TB_TERMS` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_adminAccount` ADD CONSTRAINT `PK_TB_ADMINACCOUNT` PRIMARY KEY (
	`adminId`
);

ALTER TABLE `tb_refresh_token` ADD CONSTRAINT `PK_TB_REFRESH_TOKEN` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_magazine` ADD CONSTRAINT `PK_TB_MAGAZINE` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_subscription` ADD CONSTRAINT `PK_TB_SUBSCRIPTION` PRIMARY KEY (
	`id`
);

ALTER TABLE `.tb_device` ADD CONSTRAINT `PK_.TB_DEVICE` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_media` ADD CONSTRAINT `PK_TB_MEDIA` PRIMARY KEY (
	`id`
);

ALTER TABLE `tb_Alert` ADD CONSTRAINT `PK_TB_ALERT` PRIMARY KEY (
	`id`
);

ALTER TABLE `Untitled2` ADD CONSTRAINT `PK_UNTITLED2` PRIMARY KEY (
	`seoinId`
);

ALTER TABLE `tb_category` ADD CONSTRAINT `FK_tb_magazine_TO_tb_category_1` FOREIGN KEY (
	`id2`
)
REFERENCES `tb_magazine` (
	`id`
);

ALTER TABLE `tb_interest` ADD CONSTRAINT `FK_tb_artist_TO_tb_interest_1` FOREIGN KEY (
	`artistId`
)
REFERENCES `tb_artist` (
	`id`
);

ALTER TABLE `tb_interest` ADD CONSTRAINT `FK_tb_user_TO_tb_interest_1` FOREIGN KEY (
	`userId`
)
REFERENCES `tb_user` (
	`userId`
);

ALTER TABLE `tb_sttCommand` ADD CONSTRAINT `FK_tb_artist_TO_tb_sttCommand_1` FOREIGN KEY (
	`artistId`
)
REFERENCES `tb_artist` (
	`id`
);

ALTER TABLE `tb_user_device` ADD CONSTRAINT `FK_tb_user_TO_tb_user_device_1` FOREIGN KEY (
	`userId`
)
REFERENCES `tb_user` (
	`userId`
);

ALTER TABLE `tb_user_device` ADD CONSTRAINT `FK_.tb_device_TO_tb_user_device_1` FOREIGN KEY (
	`id2`
)
REFERENCES `.tb_device` (
	`id`
);

ALTER TABLE `.tb_subscription_promotion` ADD CONSTRAINT `FK_tb_subscription_TO_.tb_subscription_promotion_1` FOREIGN KEY (
	`id`
)
REFERENCES `tb_subscription` (
	`id`
);

ALTER TABLE `tb_subscription_hist` ADD CONSTRAINT `FK_tb_user_TO_tb_subscription_hist_1` FOREIGN KEY (
	`userId`
)
REFERENCES `tb_user` (
	`userId`
);

ALTER TABLE `tb_subscription_hist` ADD CONSTRAINT `FK_tb_subscription_TO_tb_subscription_hist_1` FOREIGN KEY (
	`subscriptionId`
)
REFERENCES `tb_subscription` (
	`id`
);

ALTER TABLE `tb_magazine` ADD CONSTRAINT `FK_tb_artist_TO_tb_magazine_1` FOREIGN KEY (
	`artistId`
)
REFERENCES `tb_artist` (
	`id`
);

ALTER TABLE `tb_subscription` ADD CONSTRAINT `FK_tb_artist_TO_tb_subscription_1` FOREIGN KEY (
	`artistId`
)
REFERENCES `tb_artist` (
	`id`
);

ALTER TABLE `.tb_device` ADD CONSTRAINT `FK_tb_artist_TO_.tb_device_1` FOREIGN KEY (
	`artistId`
)
REFERENCES `tb_artist` (
	`id`
);

ALTER TABLE `tb_media` ADD CONSTRAINT `FK_tb_category_TO_tb_media_1` FOREIGN KEY (
	`categoryId`
)
REFERENCES `tb_category` (
	`id`
);

ALTER TABLE `tb_refresh_token` ADD CONSTRAINT `FK_tb_user_TO_tb_refresh_token_1` FOREIGN KEY (
	`userId`
)
REFERENCES `tb_user` (
	`userId`
);

ALTER TABLE `tb_refresh_token` ADD CONSTRAINT `FK_tb_adminAccount_TO_tb_refresh_token_1` FOREIGN KEY (
	`adminId`
)
REFERENCES `tb_adminAccount` (
	`adminId`
);

