plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    implementation(libs.jackson.kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.10")

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    api("de.slub-dresden:urnlib:2.0.1")

    testImplementation(libs.mockk)
    testFixturesApi(libs.jackson.kotlin)
    testFixturesApi(libs.jackson.datatype.jsr310)
    testFixturesApi(Junit5.api)
    testFixturesApi("org.jetbrains.kotlin:kotlin-test:2.2.10")
}
