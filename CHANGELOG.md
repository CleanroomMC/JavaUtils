# Changelog

## 2.2.0 - 2026-10-09

## Feature

- Add JavaVersion.components() and use it for Foojay queries *[commit by [@Rongmario](https://github.com/Rongmario) in [0f32ae8](https://github.com/CleanroomMC/JavaUtils/commit/0f32ae81386774fa533265c30f9b7cc7c3da0595)]*
- Add opt-in JavaFX bundling to provisioners *[commit by [@Rongmario](https://github.com/Rongmario) in [0a56323](https://github.com/CleanroomMC/JavaUtils/commit/0a563233b5b0eb870bccc00d968b75b375a40964)]*

## Bug Fix

- Rewrite JavaVersion parsing, ordering and equality *[commit by [@Rongmario](https://github.com/Rongmario) in [aedd909](https://github.com/CleanroomMC/JavaUtils/commit/aedd9095697d01d594bc950014e66977ac914481)]*
- Query Foojay with the host's real architecture, OS and C library *[commit by [@Rongmario](https://github.com/Rongmario) in [ae35ab5](https://github.com/CleanroomMC/JavaUtils/commit/ae35ab54299f9e8afd9e3e2230ee8458d6db65de)]*
- Provision into hidden staging directory and fail on truncated downloads *[commit by [@Rongmario](https://github.com/Rongmario) in [6f2ac02](https://github.com/CleanroomMC/JavaUtils/commit/6f2ac02f97d88888214436b9b5cac3877b8ab9d8)]*
- Find nested Cleanroom home installs and skip hidden directories *[commit by [@Rongmario](https://github.com/Rongmario) in [b7fe098](https://github.com/CleanroomMC/JavaUtils/commit/b7fe09869a34d0ab6791a1f42a19c5c2028a45b7)]*

## Documentation

- Tag APIs with their correct since javadoc versions *[commit by [@Rongmario](https://github.com/Rongmario) in [49f3c27](https://github.com/CleanroomMC/JavaUtils/commit/49f3c273d0816f69a480fbb3bd4eaaa1dddc7615)]*

## Testing

- Remove assertion that Tencent Kona lacks Java 25 *[commit by [@Rongmario](https://github.com/Rongmario) in [7f3c50c](https://github.com/CleanroomMC/JavaUtils/commit/7f3c50c4c8761d9a0c6a06895714eed29c4a9187)]*

## Build and Dependencies

- Use Cleanroom Conventions 1.3.1 for versioning and publishing *[commit by [@Rongmario](https://github.com/Rongmario) in [097549b](https://github.com/CleanroomMC/JavaUtils/commit/097549bf76d3c01c8e4fb22b946c04364a9e5229)]*

## CI

- Add CI and publish workflows from Conventions *[commit by [@Rongmario](https://github.com/Rongmario) in [bcfcce8](https://github.com/CleanroomMC/JavaUtils/commit/bcfcce8750bf177db25b5908fdaee34db63f9528)]*

## Other

- Support custom cleanroom home dir, for new launcher/relauncher - 2.1.6 *[commit by [@Rongmario](https://github.com/Rongmario) in [c808010](https://github.com/CleanroomMC/JavaUtils/commit/c808010c2db4250ead29ce3c3f6c450aedcc95c9)]*
- Fix mac jdk bundles not being extracted properly - Bump to 2.1.5 *[commit by [@Rongmario](https://github.com/Rongmario) in [b06c116](https://github.com/CleanroomMC/JavaUtils/commit/b06c116b171821c2efe823e600cf02ee326539c3)]*
- Fixed options arguments being picked up - Bump to 2.1.4 *[commit by [@Rongmario](https://github.com/Rongmario) in [3e13463](https://github.com/CleanroomMC/JavaUtils/commit/3e1346376084ec92797353d493376e94682e68ea)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/2.1.3...2.2.0

## 2.1.3 - 2026-07-15

## Other

- Use `-XshowSettings` as fallback - Bump to 2.1.3 *[commit by [@Rongmario](https://github.com/Rongmario) in [21b504d](https://github.com/CleanroomMC/JavaUtils/commit/21b504dd473afd0f0db2cbff47ed4621864db1d7)]*
- Stop trying to parse IntelliJ metadata *[commit by [@Rongmario](https://github.com/Rongmario) in [d4f2fd1](https://github.com/CleanroomMC/JavaUtils/commit/d4f2fd16c7675f7c568149ea6dc0c7a0b7028bb1)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/2.1.2...2.1.3

## 2.1.2 - 2026-06-30

## Other

- Use `size()` to check emptiness instead due to gson versions - 2.1.2 *[commit by [@Rongmario](https://github.com/Rongmario) in [9a7ce14](https://github.com/CleanroomMC/JavaUtils/commit/9a7ce147523a21a98ac7efe651b4fdc0b0fc99c4)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/2.1.1...2.1.2

## 2.1.1 - 2026-06-21

## Other

- Bump to 2.1.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [2045c08](https://github.com/CleanroomMC/JavaUtils/commit/2045c087d8c704e603c8391aede275ba4ff45c45)]*
- Use deprecated JsonParser instantiation for older Gson installations *[commit by [@Rongmario](https://github.com/Rongmario) in [ad2578a](https://github.com/CleanroomMC/JavaUtils/commit/ad2578abf17110d1a20369b86d4f4d4659b2d1ff)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/2.1.0...2.1.1

## 2.1.0 - 2026-06-17

## Other

- Bump to 2.1.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [b0374d3](https://github.com/CleanroomMC/JavaUtils/commit/b0374d33725287f49f4a3b956675746342657867)]*
- Impls *[commit by [@Rongmario](https://github.com/Rongmario) in [5dc6829](https://github.com/CleanroomMC/JavaUtils/commit/5dc68292067c553f228156a11d2b5efcdd563312)]*
- Download & Scan listeners *[commit by [@Rongmario](https://github.com/Rongmario) in [0476bee](https://github.com/CleanroomMC/JavaUtils/commit/0476beef234b9db97b97b8491fdb3d63f486754b)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/2.0.0...2.1.0

## 2.0.0 - 2026-06-14

> [!IMPORTANT]
> This is a new major version (1.x → 2.x).

## Other

- Fixes for maven artifacts *[commit by [@Rongmario](https://github.com/Rongmario) in [d70d745](https://github.com/CleanroomMC/JavaUtils/commit/d70d74593029eeb8f683fd05f98b53d5f07da2a6)]*
- 2.0.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [316d27f](https://github.com/CleanroomMC/JavaUtils/commit/316d27fcefab48d14c46ecbb167979cf86d22e4d)]*
- JSpecify *[commit by [@Rongmario](https://github.com/Rongmario) in [64acccd](https://github.com/CleanroomMC/JavaUtils/commit/64acccdcd4d10a1857d781defd332e6488ca5466)]*
- Tests for previous commit *[commit by [@Rongmario](https://github.com/Rongmario) in [6a54042](https://github.com/CleanroomMC/JavaUtils/commit/6a540422b51a62dbd7820186805003eb2b2aa2f1)]*
- Implement `exists` + `defaultDistro` method for JavaProvisioner *[commit by [@Rongmario](https://github.com/Rongmario) in [7fce37d](https://github.com/CleanroomMC/JavaUtils/commit/7fce37dfb069213430b9d64b03c41a06e1848761)]*
- Testing 123 *[commit by [@Rongmario](https://github.com/Rongmario) in [8d912bf](https://github.com/CleanroomMC/JavaUtils/commit/8d912bf040158606f9f8c2d39b98c265c7377a05)]*
- Added downloading test + specified versions now also work *[commit by [@Rongmario](https://github.com/Rongmario) in [db355c2](https://github.com/CleanroomMC/JavaUtils/commit/db355c277f97ca89429ffdc8df50e12d746eaaef)]*
- `FoojayJavaProvisioner` - thanks to @AnasDevO for starting it off *[commit by [@Rongmario](https://github.com/Rongmario) in [5c290d6](https://github.com/CleanroomMC/JavaUtils/commit/5c290d65465f5b2f034d114c065f02f513a075c2)]*
- Dep on gson + commons compress for archives and json parsing *[commit by [@Rongmario](https://github.com/Rongmario) in [c87750e](https://github.com/CleanroomMC/JavaUtils/commit/c87750ef3b89e296df5fd4fba8adfce2b302425b)]*
- Finalize JavaProvisioner API *[commit by [@Rongmario](https://github.com/Rongmario) in [23d5402](https://github.com/CleanroomMC/JavaUtils/commit/23d5402f38802996dc682efe1fcf94ea6844bd33)]*
- JavaLocator javadoc *[commit by [@Rongmario](https://github.com/Rongmario) in [ff474e9](https://github.com/CleanroomMC/JavaUtils/commit/ff474e921a8d4b00fe0e381b786bba173080d8c0)]*
- Throw IOException for currentJarLocation method *[commit by [@Rongmario](https://github.com/Rongmario) in [c5ff32f](https://github.com/CleanroomMC/JavaUtils/commit/c5ff32fe7f94f525290263f12aca5db4b1b33748)]*
- Throw exception if java checker encounters erroneous output *[commit by [@Rongmario](https://github.com/Rongmario) in [a8d0be5](https://github.com/CleanroomMC/JavaUtils/commit/a8d0be57d1082d8b96d28d4d8a1ecf68f2836085)]*
- Add Provisionning locator to JavaUtils *[commit by [@AnasDevO](https://github.com/AnasDevO) in [2812d56](https://github.com/CleanroomMC/JavaUtils/commit/2812d561fc2e5ae4edb1b63442ebefb197e147a2)]*
- Removed ambiguous typo *[commit by [@Rongmario](https://github.com/Rongmario) in [30a9f84](https://github.com/CleanroomMC/JavaUtils/commit/30a9f8452bc7b9fccc1612032871936362796dde)]*
- Allow queries to be made with specific locators. *[commit by [@Rongmario](https://github.com/Rongmario) in [3556406](https://github.com/CleanroomMC/JavaUtils/commit/3556406a1cd2721e79b42884ac801921f6ac0d21)]*
- Allow queries to return with multiple installs *[commit by [@Rongmario](https://github.com/Rongmario) in [b9c9965](https://github.com/CleanroomMC/JavaUtils/commit/b9c99654a779e0345d37339054ee70805149b4ba)]*
- ParseOrNull => parse *[commit by [@Rongmario](https://github.com/Rongmario) in [3c5772a](https://github.com/CleanroomMC/JavaUtils/commit/3c5772ae18c87162407ac8a48423e6a78985efdc)]*
- Add JavaVersion parsers that just take a singular int for major version *[commit by [@Rongmario](https://github.com/Rongmario) in [68dbd98](https://github.com/CleanroomMC/JavaUtils/commit/68dbd98b9e5b10ebddcabc014cf22d0264312eea)]*
- Start on JavaProvisioner *[commit by [@Rongmario](https://github.com/Rongmario) in [56cb286](https://github.com/CleanroomMC/JavaUtils/commit/56cb286f1cf90950a1c7fe4fc5c7dca153af10ed)]*
- Bounded search through program files *[commit by [@Rongmario](https://github.com/Rongmario) in [52a19c2](https://github.com/CleanroomMC/JavaUtils/commit/52a19c2ce201e6736e5574c39e6baa2c4148c62f)]*
- JavaVendor => JavaDistro, more distros, and more robust matching *[commit by [@Rongmario](https://github.com/Rongmario) in [8e6a6a1](https://github.com/CleanroomMC/JavaUtils/commit/8e6a6a18f326973de760cfc2a7073c812dd2db85)]*
- Amend and add more PrismLauncher directories *[commit by [@Rongmario](https://github.com/Rongmario) in [b408a62](https://github.com/CleanroomMC/JavaUtils/commit/b408a6252dc7e9f4b98c8081e5dd3c1e3d4687bb)]*
- Update locators to use NIO API *[commit by [@Rongmario](https://github.com/Rongmario) in [d348f00](https://github.com/CleanroomMC/JavaUtils/commit/d348f0023051559c6b793e930c676156d9981a29)]*
- Cast properly *[commit by [@Rongmario](https://github.com/Rongmario) in [40f8711](https://github.com/CleanroomMC/JavaUtils/commit/40f8711e2f7adbbea24811ce623248c5d42b0fe4)]*
- Make gradlew executable *[commit by [@Rongmario](https://github.com/Rongmario) in [bea346b](https://github.com/CleanroomMC/JavaUtils/commit/bea346b0fa5a19adcad102353bcb679007026c3e)]*
- Let's work with NIO. *[commit by [@Rongmario](https://github.com/Rongmario) in [2c7a33f](https://github.com/CleanroomMC/JavaUtils/commit/2c7a33f490ba6693c59e38e7a93cb7db1396cfd2)]*

## First-time Contributors

- **[@AnasDevO](https://github.com/AnasDevO) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.5...2.0.0

## 1.1.5 - 2025-12-04

## Other

- Bump to 1.1.5 *[commit by [@Rongmario](https://github.com/Rongmario) in [59eaf08](https://github.com/CleanroomMC/JavaUtils/commit/59eaf0882e6b52aa61c672de0590a55147b69ca3)]*
- Add PrismLauncher paths to detect from *[commit by [@Rongmario](https://github.com/Rongmario) in [d12736a](https://github.com/CleanroomMC/JavaUtils/commit/d12736ad70a15cde3074e749fb1bedb0f954fd23)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.4...1.1.5

## 1.1.4 - 2025-12-04

## Other

- Bump to 1.1.4 *[commit by [@Rongmario](https://github.com/Rongmario) in [5bc6ff9](https://github.com/CleanroomMC/JavaUtils/commit/5bc6ff9e7c8afce4109ee85cea6c895f59112d8d)]*
- Don't search the entirety of `/opt` because it may have too much on some systems *[commit by [@trustytrojan](https://github.com/trustytrojan) in [2f308fb](https://github.com/CleanroomMC/JavaUtils/commit/2f308fb3b39969f5628b536ff87f7b409032af2a)]*

## First-time Contributors

- **[@trustytrojan](https://github.com/trustytrojan) made their first contribution!**

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.3...1.1.4

## 1.1.3 - 2025-08-28

## Other

- Bump to 1.1.3 *[commit by [@Rongmario](https://github.com/Rongmario) in [45440f1](https://github.com/CleanroomMC/JavaUtils/commit/45440f1b90085a8f359735f8eca05348ba5bcd38)]*
- Fixed homebrew path not being checked if it is valid *[commit by [@Rongmario](https://github.com/Rongmario) in [91d3cc9](https://github.com/CleanroomMC/JavaUtils/commit/91d3cc91f340ae5b0187be6bb15faf618fdd72cd)]*
- Publish version without locators + source jars for api/nolocator *[commit by [@Rongmario](https://github.com/Rongmario) in [eeeebc8](https://github.com/CleanroomMC/JavaUtils/commit/eeeebc8e6ad0c987c2e34976a5b23d5fcaad8574)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.2...1.1.3

## 1.1.2 - 2025-04-10

## Other

- Bump to 1.1.2 *[commit by [@Rongmario](https://github.com/Rongmario) in [af5ca50](https://github.com/CleanroomMC/JavaUtils/commit/af5ca50b966c1a6191e1338cb70efc451863f940)]*
- Better streamlined detection of java installs, fixes #1 *[commit by [@Rongmario](https://github.com/Rongmario) in [41d2705](https://github.com/CleanroomMC/JavaUtils/commit/41d270562e4ce27f167b9ee9ac1426c85b632291), issue by [@rozbrajaczpoziomow](https://github.com/rozbrajaczpoziomow)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.1...1.1.2

## 1.1.1 - 2025-04-08

## Other

- Oops *[commit by [@Rongmario](https://github.com/Rongmario) in [c7824e5](https://github.com/CleanroomMC/JavaUtils/commit/c7824e58b4028dea567638393b52341a162f7f16)]*
- Bump to 1.1.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [9691312](https://github.com/CleanroomMC/JavaUtils/commit/96913122c17fe66250fee20e40dd42b1bcb7e057)]*
- Case when java is pointed to and not only javaw *[commit by [@Rongmario](https://github.com/Rongmario) in [b6f3f56](https://github.com/CleanroomMC/JavaUtils/commit/b6f3f56b524103369cf15c447dd0caadaa92b2f9)]*
- Better exception message *[commit by [@Rongmario](https://github.com/Rongmario) in [91a09af](https://github.com/CleanroomMC/JavaUtils/commit/91a09af40e063eb953568421db82b29cc5546142)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.1.0...1.1.1

## 1.1.0 - 2025-04-07

## Other

- Bump to 1.1.0 *[commit by [@Rongmario](https://github.com/Rongmario) in [596b3cc](https://github.com/CleanroomMC/JavaUtils/commit/596b3ccdb4f9edcd56867eef973bb1f0829317a3)]*
- Allow parsing Java 1.1 installs if you really still had that installed *[commit by [@Rongmario](https://github.com/Rongmario) in [dfc4cd5](https://github.com/CleanroomMC/JavaUtils/commit/dfc4cd53dcb413bea5e0b37030efd885e1431664)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.0.2...1.1.0

## 1.0.2 - 2025-04-01

## Other

- Bump to 1.0.2 *[commit by [@Rongmario](https://github.com/Rongmario) in [698c61e](https://github.com/CleanroomMC/JavaUtils/commit/698c61e26c6ef6b668a5e484d75611bc6e5d37da)]*
- Bring comparable impl to vendor and install objs *[commit by [@Rongmario](https://github.com/Rongmario) in [992dfcb](https://github.com/CleanroomMC/JavaUtils/commit/992dfcbc568d8b0eff6a1462a4f501d3fb265e39)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.0.1...1.0.2

## 1.0.1 - 2025-04-01

## Other

- Bump to 1.0.1 *[commit by [@Rongmario](https://github.com/Rongmario) in [b904901](https://github.com/CleanroomMC/JavaUtils/commit/b9049018bf53b3252c92306d74795fcba9d82aff)]*
- Avoid `AccessDeniedException` by walking directories w/o `Files::walk` *[commit by [@Rongmario](https://github.com/Rongmario) in [9ad2ae7](https://github.com/CleanroomMC/JavaUtils/commit/9ad2ae7bc51aef90407b634cf4d6a51b05c5fc76)]*
- JavaVendor toString + allow case-insensitive check when finding vendors *[commit by [@Rongmario](https://github.com/Rongmario) in [0445a31](https://github.com/CleanroomMC/JavaUtils/commit/0445a310f513fad03598e2f45965fc21d94af830)]*

**Full Changelog**: https://github.com/CleanroomMC/JavaUtils/compare/1.0.0...1.0.1

## 1.0.0 - 2025-04-01

## Other

- Use `File#getAbsolutePath` for hashCode as well as for equals *[commit by [@Rongmario](https://github.com/Rongmario) in [09a81d6](https://github.com/CleanroomMC/JavaUtils/commit/09a81d648837c4a182e971b39367700ff2c5a102)]*
- Add sourceJar and declare slf4j-simple as runtimeOnly *[commit by [@Rongmario](https://github.com/Rongmario) in [7b7eb53](https://github.com/CleanroomMC/JavaUtils/commit/7b7eb5361f3b4c1f204931071df3d98976eda63f)]*
- Only try parsing directories in gradle java locator *[commit by [@Rongmario](https://github.com/Rongmario) in [3e10f2b](https://github.com/CleanroomMC/JavaUtils/commit/3e10f2b37bd37dc51b441397604921e6a98c0641)]*
- Allow selection of java/javaw executables in JavaInstall API *[commit by [@Rongmario](https://github.com/Rongmario) in [0bdb20f](https://github.com/CleanroomMC/JavaUtils/commit/0bdb20f20bd800ef5925a766be14fcfe93b277e4)]*
- Fix ProgramFiles(Arm) env var causing NPE *[commit by [@Rongmario](https://github.com/Rongmario) in [82a8f01](https://github.com/CleanroomMC/JavaUtils/commit/82a8f01d5a77a89d57f3021b20b20fd86c6fca59)]*
- Use api configuration for slf4j/platform-utils *[commit by [@Rongmario](https://github.com/Rongmario) in [12822ef](https://github.com/CleanroomMC/JavaUtils/commit/12822effd45e5ed640d8e14d568e3d93ed6fb6e2)]*
- All sourcesets in one jar, along with api/checker jars *[commit by [@Rongmario](https://github.com/Rongmario) in [d781572](https://github.com/CleanroomMC/JavaUtils/commit/d781572261f61cf6d28c863b3be9a696e4054f1b)]*
- Minecraft related runtime locators *[commit by [@Rongmario](https://github.com/Rongmario) in [94ebf88](https://github.com/CleanroomMC/JavaUtils/commit/94ebf8817b68b031fd77fe85729778bc57249960)]*
- Move deep scan out into abstract class + added more linux locations *[commit by [@Rongmario](https://github.com/Rongmario) in [b675744](https://github.com/CleanroomMC/JavaUtils/commit/b675744bcaca517977f70c3666fe7d86c3cbecb8)]*
- Setup publishing and remove testing framework for time being *[commit by [@Rongmario](https://github.com/Rongmario) in [5cc262c](https://github.com/CleanroomMC/JavaUtils/commit/5cc262ccaa771755e470871323f1817d2f4c5815)]*
- Some class name changes *[commit by [@Rongmario](https://github.com/Rongmario) in [1fc51c9](https://github.com/CleanroomMC/JavaUtils/commit/1fc51c9d3a9d8102d6fc4523e4d33ddde06d2ebd)]*
- Deduplicate parsing code *[commit by [@Rongmario](https://github.com/Rongmario) in [1eb046a](https://github.com/CleanroomMC/JavaUtils/commit/1eb046a81d454387180e07cc8e37b50624133e25)]*
- SDKMan + Homebrew support *[commit by [@Rongmario](https://github.com/Rongmario) in [28f401e](https://github.com/CleanroomMC/JavaUtils/commit/28f401e3696f49476af9cdce8ae6d3e6e6bd7c25)]*
- Locate default install directories for windows/mac/linux *[commit by [@Rongmario](https://github.com/Rongmario) in [cce4003](https://github.com/CleanroomMC/JavaUtils/commit/cce4003ede2c94d0dafbd5295c87451f38e67bd7)]*
- Complete WindowsRegistryJavaLocator *[commit by [@Rongmario](https://github.com/Rongmario) in [4024e04](https://github.com/CleanroomMC/JavaUtils/commit/4024e047e02cc0804e09acffbb5858e27664b090)]*
- Simplified JavaVersion parsing and allow it to parse older versions *[commit by [@Rongmario](https://github.com/Rongmario) in [59d52ba](https://github.com/CleanroomMC/JavaUtils/commit/59d52ba8b72713359cb35ab7bf93feedb1ab9cc7)]*
- JavaVendor API *[commit by [@Rongmario](https://github.com/Rongmario) in [d8f392a](https://github.com/CleanroomMC/JavaUtils/commit/d8f392a6b52131abfd6fb8a9a35d416817802ba1)]*
- Filter for non nulls when compiling installs list *[commit by [@Rongmario](https://github.com/Rongmario) in [5c5cf98](https://github.com/CleanroomMC/JavaUtils/commit/5c5cf98816acdf078b25217ca271fe4f62359857)]*
- Slf2j + better exception handling + windows registry query *[commit by [@Rongmario](https://github.com/Rongmario) in [a195512](https://github.com/CleanroomMC/JavaUtils/commit/a1955122cc0ee41e642b745e3ad69db76dddaf81)]*
- Change the tests to reflect previous commit change *[commit by [@Rongmario](https://github.com/Rongmario) in [bf7d3a9](https://github.com/CleanroomMC/JavaUtils/commit/bf7d3a9957ba619282dab56e7946de08652ca359)]*
- Differentiate JAVA_HOME env and java.home property *[commit by [@Rongmario](https://github.com/Rongmario) in [af014dc](https://github.com/CleanroomMC/JavaUtils/commit/af014dcc7b4cc8e2b130a631513c9bd026c86be5)]*
- Jabba + set as api + hashCode/equals impl for internal JavaInstall *[commit by [@Rongmario](https://github.com/Rongmario) in [2e96c9d](https://github.com/CleanroomMC/JavaUtils/commit/2e96c9da3ee31159008d30416ef2bd8b1f6b5d3b)]*
- IntelliJ + ASDF *[commit by [@Rongmario](https://github.com/Rongmario) in [ceec263](https://github.com/CleanroomMC/JavaUtils/commit/ceec2630b1a729545f172a575bcdf0ff997fcdcd)]*
- Split checker into its own sourceset for older java compatibility *[commit by [@Rongmario](https://github.com/Rongmario) in [9b91652](https://github.com/CleanroomMC/JavaUtils/commit/9b9165217a9f4141637db23bdc414e7ac8c35b47)]*
- Added locator for gradle provisioned jdks *[commit by [@Rongmario](https://github.com/Rongmario) in [76a146c](https://github.com/CleanroomMC/JavaUtils/commit/76a146c78b1db1996ae8e866956c1c3e59c38302)]*
- HashCode, equals implementations for JavaVersion *[commit by [@Rongmario](https://github.com/Rongmario) in [23c8688](https://github.com/CleanroomMC/JavaUtils/commit/23c8688c40d510625db76024583a37815cab51a5)]*
- Proper JavaVersion and implemented first locator *[commit by [@Rongmario](https://github.com/Rongmario) in [88f6f6f](https://github.com/CleanroomMC/JavaUtils/commit/88f6f6f2421a4113eb955837963c565152c67cf5)]*
- Push before disappearing for a whole day *[commit by [@Rongmario](https://github.com/Rongmario) in [4912179](https://github.com/CleanroomMC/JavaUtils/commit/4912179a46502d0c199df4fabe17eaba803e4fcf)]*


