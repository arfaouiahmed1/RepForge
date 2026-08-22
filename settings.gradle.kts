pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RepForge"

include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":core:health")
include(":core:ml")
include(":core:analytics")
include(":core:testing")
include(":feature:onboarding")
include(":feature:today")
include(":feature:workout")
include(":feature:routine")
include(":feature:progress")
include(":feature:settings")
include(":baselineprofile")


include(":feature:lab")
include(":feature:paywall")
include(":feature:formlab")
include(":wear")
include(":core:notifications")
include(":core:threeD")
include(":feature:achievements")
include(":feature:exercise")
