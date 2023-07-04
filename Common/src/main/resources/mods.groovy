ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[40,)'

    license = 'MIT'

    mod {
        modId = 'placebo'
        displayName = 'Placebo'

        version = this.version

        description = 'Nice library mod'
        authors = [
                'Shadows'
        ]

        dependencies {
            minecraft = this.minecraftVersionRange

            onFabric {
                fabricLoader = ">=${this.fabricLoaderVersion}"
                mod('fabric') {
                    mandatory = true
                    versionRange = ">=${this.buildProperties.fabric_version}"
                }
            }

            onForge {
                forge = ">=${this.forgeVersion}"
            }
        }

        entrypoints {
            main = 'shadows.placebo.fabric.PlaceboFabric'
            client = 'shadows.placebo.fabric.client.PlaceboFabricClient'
        }
    }

    mixin = "${buildProperties.mod_id}.mixins.json"
    onFabric {
        mixin = "${buildProperties.mod_id}_fabric.mixins.json"
    }
}