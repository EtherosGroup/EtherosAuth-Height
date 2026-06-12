plugins {
    kotlin("jvm")
}

tasks.register("prepareKotlinBuildScriptModel")

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("cn.skilfully.etheros:EtherosFramework-Yosemite:1.1.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.hibernate.orm:hibernate-core:6.4.4.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:6.4.4.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.4.4.Final")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.slf4j:slf4j-nop:2.0.13")
    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    mergeServiceFiles()
}
