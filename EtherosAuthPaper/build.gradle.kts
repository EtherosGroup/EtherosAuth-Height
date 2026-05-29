plugins {
    kotlin("jvm")
}

tasks.register("prepareKotlinBuildScriptModel")

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("cn.skilfully.etheros:EtherosFramework-Yosemite:1.0.7")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
}
