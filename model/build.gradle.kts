dependencies {
    implementation(Jackson.kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("com.github.navikt:pam-geography:2.15")

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    implementation("de.slub-dresden:urnlib:2.0.1")
    testImplementation(Mockk.mockk)
}
