# Changelog

## v1.1.0 - Create 机械动力联动与信息类模组兼容更新

> Smoking Warning Mod v1.1.0 — Create Mechanical Power Integration & Recipe Viewer Compatibility Update

### 新增内容 / New Features

- **Create / 机械动力可选兼容** — 安装 Create 6.0.10+ 后，玩家可获得完整的机械化烟草加工流水线。
- **机械化烟草加工流水线** — 包含风扇烘干、石磨研磨、粉碎轮加工、机械压制、机械手部署、序列组装等全套自动化工序。
- **新增中间物品** — 粗烟丝（Coarse Tobacco Shreds）、精制烟丝（Refined Tobacco Shreds）、卷烟纸（Cigarette Paper）、过滤嘴（Cigarette Filter）、未完成香烟（Unfinished Cigarette）、包装香烟（Packaged Cigarette）。
- **机械风扇烘干烟草叶** — 使用 Create 鼓风机 + 火/营火批量烘干烟草叶。
- **石磨 / 粉碎轮加工** — 干烟草叶可通过 milling 或 crushing 机械加工为粗烟丝，产量高于手工路线。
- **机械压制卷烟纸** — 纸经过 pressing 压制成卷烟纸。
- **机械混合制作过滤嘴** — 线 + 卷烟纸通过 mixing 制作过滤嘴。
- **序列组装制作香烟** — 卷烟纸 + 精制烟丝 + 过滤嘴经过多步 sequenced_assembly 制作未完成香烟，再压制为成品香烟。
- **高级彩蛋香烟机械路线** — 瑞克五代（7 步序列组装）、华子香烟（4 步）、荷花香烟（4 步）拥有完整的机械自动化生产流程。
- **变体香烟机械配方** — 所有变体香烟均支持机械手部署 + 压力机压制的两步序列组装。
- **REI 可选兼容** — 新增 REI（Roughly Enough Items）插件，支持在 REI 中查看晾晒架、研磨台、烟草工作台的自定义配方。
- **完善 JEI 兼容** — 新增 JEI 插件，支持在 JEI 中按分类查看所有自定义配方。
- **完善 Jade 兼容** — 晾晒架、研磨台、烟草工作台均支持 Jade 信息显示。

### 优化内容 / Improvements

- Create 机械动力路线作为额外自动化路线，不替代原有手工烟草加工方式。
- 原有晾晒架、研磨台、烟草工作台路线完全保留，适合游戏前期使用。
- 机械配方步骤更多、更复杂，但产量更高，适合中后期大规模自动化。
- 高级香烟（瑞克五代、华子、荷花等）不再只是简单合成，而是需要多步骤机械加工。
- 优化 JEI / REI 配方分类展示，便于查看不同加工路线的配方。
- 优化 README 和模组介绍文本，补充 Create 兼容说明。

### 修复内容 / Bug Fixes

- 修复部分高级香烟在 JEI / EMI 中看不到配方的问题。
- 修复瑞克五代、华子香烟、荷花香烟配方在 JEI 中显示异常。
- 修复 REI 无法显示本模组自定义配方的问题（需同时安装 REI 16.x + Architectury + Cloth Config）。
- 修复可选兼容模组未安装时可能出现的 ClassNotFoundException 或崩溃问题。
- 修复 Create 配方在未安装 Create 时产生 JSON 解析错误的问题（已添加 neoforge:mod_loaded 条件加载）。
- 修复包装香烟拆解配方堆叠数超出限制的问题。

### 兼容性说明 / Compatibility

| 项目 | 说明 |
|------|------|
| Minecraft | 1.21.1 |
| Mod Loader | NeoForge |
| Java | 21 |
| Create | 可选兼容（Optional） |
| JEI | 可选兼容（Optional） |
| EMI | 可选兼容（Optional） |
| REI | 可选兼容（Optional） |
| Jade | 可选兼容（Optional） |

**以上所有第三方模组均为可选兼容，不是必装前置。** 不安装任何第三方模组时，本模组仍可正常游玩。

### 技术说明 / Technical Notes

- Create 机械配方使用 `neoforge:mod_loaded` 条件加载，未安装 Create 时不会产生 JSON 解析错误。
- JEI 插件使用 `@JeiPlugin` 注解注册，仅在 JEI 安装时由 JEI 自动发现。
- REI 插件使用 `@REIPluginClient` 注解注册，仅在 REI 安装时由 REI 自动发现。
- 所有可选兼容插件的 API 依赖均为 `compileOnly`，不会打包进本模组 JAR。
- 专用服务器（Dedicated Server）不会因 REI/JEI 客户端类而崩溃。
