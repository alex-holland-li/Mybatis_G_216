plugins {
  id("java")
  id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.liyun"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// 配置 Gradle IntelliJ Plugin
intellij {
  // 指定 IntelliJ IDEA 的版本
  version.set("2022.2.5") // 根据你的开发环境调整版本
  type.set("IU") // 'IC' 表示 Community Edition，'IU' 表示 Ultimate Edition

  // 添加插件依赖，这里添加官方数据库插件
  plugins.set(listOf("com.intellij.database"))
}

dependencies {
  // 添加 FreeMarker 依赖
  implementation("org.freemarker:freemarker:2.3.31")
}

tasks {
  // 设置 JVM 兼容性版本
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  // 配置 patchPluginXml 任务
  patchPluginXml {
    sinceBuild.set("222") // 对应 IntelliJ IDEA 2022.2
    untilBuild.set("232.*") // 直到 IntelliJ IDEA 2023.2.x
    // 可选：添加变更日志
    // changeNotes.set("""
    //     初始版本发布。
    // """.trimIndent())
  }

  // 配置插件签名任务
  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  // 配置插件发布任务
  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
    // 可选：指定发布渠道，例如 beta
    // channels.set(listOf("beta"))
  }

  // 确保 buildSearchableOptions 任务依赖于 compileJava
  named("buildSearchableOptions") {
    dependsOn("compileJava")
  }
}
