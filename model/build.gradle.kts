plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    implementation(libs.jackson.kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.3.0")

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    api("de.slub-dresden:urnlib:3.0.0")

    testImplementation(libs.mockk)
    testFixturesApi(libs.jackson.kotlin)
    testFixturesApi(libs.jackson.datatype.jsr310)
    testFixturesApi("org.junit.jupiter:junit-jupiter-api:6.0.3")
    testFixturesApi("org.jetbrains.kotlin:kotlin-test:2.3.0")
}
