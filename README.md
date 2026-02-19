# Limae
Limae is a local-first text polishing tool that corrects and refines your writing without relying on cloud-based generative AI. Built with Kotlin and Compose Multiplatform, it runs across Android, Desktop, and Web, wrapping multiple open-source linguistic tools under one interface.

|            Android             | Desktop | Web |
|:------------------------------:| :---: | :---: |
| ![Android](assets/android.png) | ![Desktop](assets/desktop.png) | ![Web](assets/web.png) |

* **Deterministic Editing:** Limae strictly edits and corrects. It does not hallucinate information, alter your intended meaning, or generate new content from scratch, since the engines it uses are not even capable of that.
* **Current Engine:** Harper is currently integrated as the default engine for all platforms, while LanguageTool is additionally integrated for desktop targets.
* **Zero Telemetry:** Runs as a system-wide overlay on Android via accessibility service, like the Grammarly app, except your data stays on your device. Never collected.

### Development & Licensing

Still in active development. No ETA on the first release.

`base` is the main branch. Once the first release is out, things will happen on the `dev` branch, later merged to the `base` branch.

All client applications within the `@LimaeApp` organization will be licensed under GPLv3.

Limae will not include any Todo or typical note-taking app features nor any integrations with cloud-based generative LLMs. Use Joplin or Bundled Notes for note-taking. Opening issues or pull requests regarding these is a waste of time and will not be merged or acknowledged.

Limae doesn't have an icon yet. So that's that.

*Note: This repository might be transferred to the `@LimaeApp` organization in the future since other components might be added to this project, especially a dedicated language server.*

While you are here, do yourself a favor and listen to *Modal Soul* by Nujabes.