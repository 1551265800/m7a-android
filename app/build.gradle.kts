import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// ---------- 发布签名 ----------
// 原仓库没有 signingConfigs，assembleRelease 出来的是未签名包，手机上装不上。
//
// 凭据来源优先级：环境变量 > 仓库根目录的 keystore.properties > 空。
// keystore.properties 与 .jks 都已被 .gitignore 排除，密码不会进版本库。
//   storeFile / storePassword / keyAlias / keyPassword
// 找不到 keystore 时保持未签名，不影响 assembleDebug。
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties()
if (keystorePropsFile.isFile) {
    keystorePropsFile.inputStream().use { keystoreProps.load(it) }
}

fun signingValue(propKey: String, envKey: String, fallback: String = ""): String =
    System.getenv(envKey)?.takeIf { it.isNotEmpty() }
        ?: keystoreProps.getProperty(propKey)?.takeIf { it.isNotEmpty() }
        ?: fallback

val releaseKeystore = rootProject.file(
    keystoreProps.getProperty("storeFile")?.takeIf { it.isNotBlank() } ?: "m7a-release.jks"
)
val hasReleaseKeystore = releaseKeystore.isFile

android {
    // 代码包名（R 类、清单里 `.MainActivity` 这类相对名都基于它）。
    // 这个可以随便改。
    namespace = "com.miguanm7a.hsr"

    compileSdk = 37
    ndkVersion = "29.0.14206865"

    defaultConfig {
        // 包名是 com.m7ahsr（10 字符）。
        //
        // ⚠️ 这个长度不是随便定的，是**硬上限**：
        //
        // 内置 Termux bootstrap 的二进制把 /data/data/<包名>/files/usr 作为
        // $PREFIX 硬编码在 ELF 的 .dynstr 里（DT_RUNPATH 指向它）。字符串表里
        // 所有动态符号按偏移量引用，所以该字符串**只能改短、不能改长** —— 改长
        // 会让后面全部错位、整张符号表作废。
        //
        //     len("/data/data/") + len(包名) + len("/files/usr") = 21 + len(包名)
        //     原前缀 31 字节  =>  len(包名) <= 10
        //
        // bootstrap 资产已用 scripts/reprefix_bootstrap.py 重打前缀
        // （等长纯字节替换，无重定位）。**改这一行必须同时：**
        //   1. 重跑 reprefix_bootstrap.py 更新 assets/bootstrap/*.zip
        //   2. 同步 TermuxPaths.APP_PACKAGE
        // 否则 bash/proot 找不到自己的库，表现为「点了没反应」。
        //
        // TermuxPrefixInvariantTest 守着这个一致性。
        applicationId = "com.m7ahsr"

        minSdk = 24
        // Termux 本身 targetSdk 28。保持 28 才允许从应用私有目录 exec 二进制，
        // 并沿用旧版 /sdcard 存储模型 —— 这是整套方案能跑起来的前提。
        targetSdk = 28

        versionCode = 5
        versionName = "1.0.4"

        ndk {
            // 内置 bootstrap 与容器镜像都是 aarch64，其它架构需各自重建资产
            abiFilters += "arm64-v8a"
        }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = signingValue("storePassword", "M7A_STORE_PASS")
                keyAlias = signingValue("keyAlias", "M7A_KEY_ALIAS", "m7a")
                keyPassword = signingValue("keyPassword", "M7A_KEY_PASS")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // BuildConfig.APPLICATION_ID / VERSION_NAME / DEBUG 都在用
        buildConfig = true
    }

    androidResources {
        // 两个大资产必须原样存储，不能被 AGP 再压一次。
        // zip：bootstrap 压缩包；bin：526MB 的 rootfs（本身已是 gzip，
        // 再 deflate 只会白白多花几分钟，且 RootfsInstaller 用 openFd 取长度会失败）。
        noCompress += listOf("zip", "bin")
    }

    packaging {
        jniLibs {
            // 保持 .so 未压缩，targetSdk 28 下可从私有目录直接 exec
            useLegacyPackaging = true
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        // AGP 9 的 K2 UAST 在分析 .gradle.kts 时会崩
        // (findFirCompiledSymbol on non-compiled declaration)，
        // 导致 release 构建的 lintVitalRelease 失败。故关闭。
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // 内置终端（Termux 上游模块，Apache-2.0）
    implementation(project(":terminal-emulator"))
    implementation(project(":terminal-view"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // 实时监看：用 OkHttp 的 WebSocket 连 Chrome DevTools Protocol
    implementation(libs.okhttp)

    // 测试直接读真实的 bootstrap / rootfs 资产做校验
    testImplementation(libs.junit)
    testImplementation(libs.commons.compress)

    debugImplementation(libs.androidx.ui.tooling)
}

// ---------- 运行时资产守卫 ----------
// 仓库不含这两个文件（合计约 537MB）。缺了照样能编译出 APK，但那是空壳：
// 装到手机上点「开始部署」，解压 bootstrap 的第一步就会失败。
// 这里在构建期明确报出来：release 缺资产直接失败，debug 只警告
// （方便在资产就位前先调界面）。
val runtimeAssets = listOf(
    "bootstrap/termux-bootstrap.zip" to 30L * 1024 * 1024,
    "rootfs/m7a-rootfs.bin" to 400L * 1024 * 1024,
)

fun registerRuntimeAssetCheck(taskName: String, strict: Boolean) = tasks.register(taskName) {
    group = "verification"
    description = if (strict) "校验运行时资产（缺失即失败）" else "校验运行时资产（缺失仅警告）"
    val assetsDir = project.layout.projectDirectory.dir("src/main/assets")
    doLast {
        val problems = runtimeAssets.mapNotNull { (relative, minSize) ->
            val file = assetsDir.file(relative).asFile
            when {
                !file.isFile -> "缺少 $relative"
                file.length() < minSize ->
                    "体积异常 $relative（${file.length() / 1048576}MB，至少应有 ${minSize / 1048576}MB）"
                else -> null
            }
        }
        if (problems.isEmpty()) return@doLast
        val message = buildString {
            appendLine("运行时资产不完整，当前打出来的 APK 是空壳（部署第一步必然失败）：")
            problems.forEach { appendLine("  - $it") }
            appendLine("生成方法见 scripts/README.md，或把已生成的文件放到 app/src/main/assets/ 对应目录：")
            appendLine("  python scripts/pull_image.py")
            appendLine("  python scripts/assemble_rootfs.py")
            appendLine("  python scripts/build_bootstrap.py")
        }
        if (strict) throw GradleException(message) else logger.warn(message)
    }
}

registerRuntimeAssetCheck("verifyRuntimeAssetsStrict", strict = true)
registerRuntimeAssetCheck("verifyRuntimeAssets", strict = false)

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn("verifyRuntimeAssetsStrict")
}
tasks.matching { it.name == "preDebugBuild" }.configureEach {
    dependsOn("verifyRuntimeAssets")
}
