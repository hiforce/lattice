# Lattice Framework Introduction

Lattice is a powerful, lightweight business extension invoke framework. By using the Lattice framework, complex business customization can be efficiently organized and managed.

The main ideas of Lattice's architecture design are:

* **The plug-in architecture that separates the business package from the platform:** The platform provides a plug-in package registration mechanism to realize the registration of the business-side plug-in package during runtime. The business code is only allowed to exist in the plugin package and is strictly separated from the platform code. The code configuration library of the business package is also separated from the code library of the platform, and is provided to the container for loading through the second-party package.
* **Unified business identity across the full-chain:** The platform needs to have the ability to logically isolate business from business according to "business identity", rather than the traditional SPI architecture that does not distinguish between business identities and simply filters. How to design this business identity has also become the key to the isolation architecture between businesses.
* **Separation of management domain and running domain:** Business logic cannot rely on dynamic calculation at run time, but can be defined and visualized at static time. The rules that appear in the business definition are superimposed and conflicted, and conflict decisions are also made in the static device. During the runtime, it is executed strictly according to the business rules and conflict decision policies defined by the static device.

# Development Guide

* [Quickstart Guide](https://github.com/hiforce/lattice/wiki/Quickstart-Guide)
* [Business Overlay Product](https://github.com/hiforce/lattice/wiki/Business-Overlay-Product)
* [Register Business Configuration](https://github.com/hiforce/lattice/wiki/Register-Business-Configuration)
* [Load Local Configuration](https://github.com/hiforce/lattice/wiki/Load-Local-Configuration)
* [Reduce Strategy](https://github.com/hiforce/lattice/wiki/Reduce-Strategy)
* [UseCase Precipitation and Reuse](https://github.com/hiforce/lattice/wiki/UseCase-Precipitation-and-Reuse)
* [RPC Invoke Extension](https://github.com/hiforce/lattice/wiki/RPC-Invoke-Extension)


------------
Answer group in DingTalk （https://www.dingtalk.com/）

![1722877540475](https://github.com/user-attachments/assets/970d6c9a-cb3d-46b7-aa17-98ee3c918b00)
