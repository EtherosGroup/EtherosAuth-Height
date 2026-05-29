plugins {
    kotlin("jvm")
}

tasks.register("prepareKotlinBuildScriptModel")

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
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
