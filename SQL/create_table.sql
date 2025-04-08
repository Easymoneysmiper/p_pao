create table if not exists user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '昵称',
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '头像',
    gender       tinyint                            null comment '性别',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment ' 状态 0--正常',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 null comment '用户权限  0--默认权限  1--管理员',
    planetCode   varchar(512)                       null comment '星球编号'
)
    comment '用户';


##标签表
CREATE TABLE tag (
                     id          BIGINT AUTO_INCREMENT COMMENT 'id',
                     tagName     VARCHAR(255) NULL COMMENT '标签名称',
                     userId      BIGINT NULL COMMENT '用户 id',
                     parentId    BIGINT NULL COMMENT '父标签 id',
                     isParent    TINYINT NULL COMMENT '0 - 不是，1 - 父标签',
                     createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '设置时间',
                     updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
                     isDelete    TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                     PRIMARY KEY (id),
                     UNIQUE INDEX uniIdx_tagName (tagName)
) COMMENT '标签';

CREATE INDEX idx_userId ON tag (userId);

##队伍表
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称 ',
    description varchar(1024)                      null comment '描述 ',
    maxNum      int      default 1                 not null comment '最大人数 ',
    expireTime  datetime                           null comment '过期时间 ',
    userId      bigint comment '用户 id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 -加密',
    password    varchar(512)                       null comment ' 密码 ',
    createTime  datetime default CURRENT_TIMESTAMP null comment ' 创建时间 ',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment ' 是否删除 '

)
    comment  '队伍';

##user_team
create table user_team
(
    id  bigint auto_increment comment 'id'
        primary key,
    userId bigint comment ' 用户 id',
    teamId  bigint comment ' 队伍 id',
    joinTime datetime null comment ' 加入时间 ',
    createTime      datetime default CURRENT_TIMESTAMP null comment' 创建时间 ',
    updateTime
    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete tinyint default 0 not null comment '是否删除'

)comment'用户队伍关系'
