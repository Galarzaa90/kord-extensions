plugins {
	`maven-publish`
	signing
}

val sourceJar: Task by tasks.getting
val javadocJar: Task by tasks.getting
//val dokkaJar: Task by tasks.getting

afterEvaluate {
	publishing {
		repositories {
			maven {
				name = "KordEx"

				url = if (project.version.toString().contains("SNAPSHOT")) {
					uri("https://repo.kordex.dev/snapshots/")
				} else {
					uri("https://repo.kordex.dev/releases/")
				}

				credentials {
					username = project.findProperty("ossrhUsername") as String?
						?: System.getenv("KORDEX_MAVEN_USERNAME")

					password = project.findProperty("ossrhPassword") as String?
						?: System.getenv("KORDEX_MAVEN_PASSWORD")
				}

				version = project.version
			}
		}

		publications {
			create<MavenPublication>("maven") {
				from(components.getByName("java"))

				artifact(sourceJar)
				artifact(javadocJar)
//                artifact(dokkaJar)

				pom {
					name.set(project.ext.get("pubName").toString())
					description.set(project.ext.get("pubDesc").toString())

					url.set("https://kordex.dev")

					packaging = "jar"

					scm {
						connection.set("scm:git:https://github.com/Kord-Extensions/kord-extensions.git")
						developerConnection.set("scm:git:git@github.com:Kord-Extensions/kord-extensions.git")
						url.set("https://github.com/Kord-Extensions/kord-extensions.git")
					}

					licenses {
						license {
							name.set("European Union Public Licence Version 1.2")
							url.set("https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12")
						}
					}

					developers {
						developer {
							id.set("gdude2002")
							name.set("Gareth Coles")
						}
					}
				}
			}
		}
	}

	signing {
		val signingKey: String? by project ?: return@signing
		val signingPassword: String? by project ?: return@signing

		useInMemoryPgpKeys(signingKey, signingPassword)

		sign(publishing.publications["maven"])
	}
}
