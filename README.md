# Magellan2 Extensions Plugins

Here you can find some additional plugins, that you can drop into Magellan2.

Please have a look into the subfolders for more informations.

- [Alliance Plugin](allianceplugin/)
- [Lighthouse Plugin](LightHouseIcons/)
- [Map Cleaner Plugin](MapCleaner/)
- [MapIcons Plugin](MapIcons/)
- [MemoryWatch Plugin](MemoryWatch/)
- [ShipLoader Plugin](ShipLoader/)
- [Statistics Plugin](statistics/)
- [Teacher Plugin](Teacher/)

As a template for new plugins, see [Plugin Template](PluginTemplate/).


## How To Compile

Actually the Magellan Plugins are very old.At the moment of writing, we do support only Java11, even when Java24 is already available. The limitatin is currently set by the Izpack installer in. We are working on that, and try to upgrade that dependency and can switch to a newer Java version.

With (f.e.) Eclipse IDE it is very easy to import this repository as a project. Also running the project with newer JDKs is not an issue. It works out of the box

- checkout the source dependency from https://github.com/magellan2/magellan2
- checkout the sources from https://github.com/magellan2/magellan2-extensions-plugins
- in Eclipse or your prefered Java IDE open the project from file system.. and import any project as a sub project
- maybe add your current JRE system library to the Eclipse project. 

To build a complete new release, we are still using Apache Ant and Java 11. To install Ant follow the instructions on https://ant.apache.org/. I'm using Homebrew with `brew install ant`. Worked as expected. But it's also included in this repository

There is only one useful target within the ant configuration:

    ant -noinput -buildfile build.xml distribute

It calls every subproject and creates the corresponding installer.

## How To Run

When the installer jars are generated, you can start them. The installer asks for the installation directory of your Magellan2 client. Point to that directory and let the installer copy the plugin into the Magellan2 client directory.

## How To Release

The process of new creating a new release is currently a bit difficulty.

- we protect the default branch, so it's not possible to commit anything to it without a merge request. When you develop a new feature or a fix a bug, you should create at least one new branch (like `develop`). Every changes must be done there. We have created a Github action that runs on your branch to test, if the application can be compiled.
- then you need to manually update `build.xml` and update the properties `VERSION.MAJOR`, `VERSION.MINOR` and `VERSION.SUB` as the Semantic versioning says. `VERSION.SUB` should increase for any bugfix release, `VERSION.MINOR` should be increased for any feature release and `VERSION.MAJOR` should be increased on major changes.
- validate, if also the izpack-install.template.xml is up2date with the new version number etc.
- commit and push everything into your branch
- create a pull request
- when the PR is approved, the build pipeline should run again and should create a stable release, see `.github/workflow/publish.yml`. It contains installer for Java. It also updates the home page and informs users about a new release.

At the moment, every plugin has it's own version number. And Magellan2 client also has a version number. There is no concrete fit between all of that. So take care, which plugin version do you install into which Magellan version.

## How to Distribute

Please feel free to distribute your ideas. Just create a Pull-Request like described above. Update `build.xml` and `README.md`. 
