plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    implementation(Jackson.kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.10")

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    api("de.slub-dresden:urnlib:2.0.1")
    testImplementation(Mockk.mockk)

    testFixturesApi(Jackson.kotlin)
    testFixturesApi(Jackson.jsr310)
    testFixturesApi(Junit5.api)
    testFixturesApi("org.jetbrains.kotlin:kotlin-test:1.9.0")
}
