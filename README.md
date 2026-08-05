# NoteBlockAPI (Folia)

> 一个让插件能向玩家播放 `.nbs` 音符盒歌曲的开发 API。
> 本仓库是 [koca2000/NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI) 的 **Folia 适配分支**，使其能够在 Folia（区域化多线程）服务器上运行，同时向下兼容普通 Paper。

A developer API for playing `.nbs` note-block songs to players — **ported to Folia**, while remaining compatible with classic Paper.

---

## 这个分支做了什么 · What's in this fork

- ✅ **Folia 支持**：新增 `FoliaCompat` 调度桥，把对 Bukkit 调度器的所有调用透明地路由到 Folia 的 Global / Region / Async / Entity Scheduler（在普通 Paper 上则回落到经典调度器）。
- ✅ **线程安全的播放**：歌曲播放从共享异步线程池改为**可追踪的专用线程**，`playTick` 按**每个听者玩家的区域线程**派发，杜绝跨 region 的实体/方块访问违例。
- ✅ **事件区域化**：`SongEnd`、`SongStopped` 等事件按**歌曲所在区域**触发（方块播放器走方块 region、实体播放器走实体 region），让下游插件能安全地读写对应方块/实体。
- ✅ **干净的启停**：移除了 Folia 上会抛 `UnsupportedOperationException` 的 `BukkitScheduler.cancelTasks/getActiveWorkers`，改为显式取消与中断播放线程。
- ✅ **依赖升级**：编译目标 Java 21、Paper API 1.21.1；`plugin.yml` 增加 `folia-supported: true`。

English: a transparent Paper/Folia scheduler bridge, per-listener region-threaded playback with tracked song threads, region-aware event dispatch, clean enable/disable without the removed BukkitScheduler APIs, and a Java 21 / Paper API 1.21.1 build marked `folia-supported`.

## 运行环境 · Requirements

| 组件 | 版本 |
|------|------|
| 服务端 | Folia 26.x（或 Paper 1.21+） |
| Java | 21+ |

## 构建 · Build

```bash
mvn clean install
```

产物在 `target/NoteBlockAPI-*-SNAPSHOT.jar`（已 shade bstats）。`install` 会把它装入本地 Maven 仓库，供 [MusicBox (Folia)](https://github.com/tiancuocuo/MusicBox-Folia) 等下游插件引用。

## 用法 · Usage

在插件的 `plugin.yml` 中声明 `depend: [NoteBlockAPI]`，然后使用 `SongPlayer` API 播放 `.nbs` 歌曲。详见 [Spigot 资源页](https://www.spigotmc.org/resources/noteblockapi.19287/)。

## 许可与归属 · Credits & License

- 原作：xxmicloxx 等；分支：[koca2000/NoteBlockAPI](https://github.com/koca2000/NoteBlockAPI)
- 许可证：**LGPL v3**（见 [LICENSE](LICENSE)），本分支沿用原许可证。
- 本分支仅做 Folia 适配，版权归原作者所有。
