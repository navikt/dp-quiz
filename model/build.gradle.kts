dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.5.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")

    testImplementation("guru.nidi:graphviz-java:0.18.1")
    testImplementation("guru.nidi:graphviz-kotlin:0.18.1")
    testImplementation(Mockk.mockk)
}
