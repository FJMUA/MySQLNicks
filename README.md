# MySQLNicks

Forked from IgnitusCo/MySQLNicks/fork, without CMI, supports high version servers

## Features
- [x] 指令支持权限判断，支持配置扣除的金币数
- [x] 支持储存 + 查询改名记录进行溯源，但仅提供溯源功能，历史名称不绑定到玩家，其他玩家也可使用
- [x] 提供 placeholder 变量，供给其他插件使用以显示玩家中文名称，并通过 mysql 进行跨服同步（各子服安装，不提供代理端实现）

## Placeholder

- `%mysqlnicks_nickname%`
  - permission:
    - `mysqlnicks.bypass.nocolor`
    - `mysqlnicks.bypass.limit`
- `%mysqlnicks_nocolor%` / `%mysqlnicks_nocolour%`
  - permission: 
    - `mysqlnicks.bypass.nocolor`
    - `mysqlnicks.bypass.limit`

## Command

- `/nick <nickname/off>` Set your nickname or remove it
  - permission: 
    - `mysqlnicks.nick`
    - `mysqlnicks.nick.color`
    - `mysqlnicks.nick.color.simple`
    - `mysqlnicks.nick.color.hex`
    - `mysqlnicks.nick.format`
    - `mysqlnicks.nick.magic`
- `/nick <player> <nickname/off>` Edit another player's nickname
  - permission: 
    - `mysqlnicks.staff`