package in.oss.docker.manager.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

case class AppConfig(emberServerConfig: EmberServerConfig) derives ConfigReader
