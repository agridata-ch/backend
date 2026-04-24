## [1.9.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.9.0-rc.1...v1.9.0-rc.2) (2026-04-24)

### Features

* **contract:** Give provider and admin access to contract revision PDF endpoint. ([db9627f](https://github.com/agridata-ch/backend/commit/db9627f1ea1b55694babe9eb2252f005ca2f470a)), closes [DIGIB2-1203](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1203)
* **deps:** Update quarkus to 3.34.6 ([f8b53d0](https://github.com/agridata-ch/backend/commit/f8b53d00116a311a93b877954c4deb6789860f82)), closes [DIGIB2-1203](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1203)

## [1.9.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.8.0...v1.9.0-rc.1) (2026-04-22)

### Features

* **aws:** add quarkus amazon services for s3 and sns ([5e41606](https://github.com/agridata-ch/backend/commit/5e41606fe7547f10760d8e881e0e62017e78f760)), closes [DIGIB2-1301](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1301)

## [1.8.0](https://github.com/agridata-ch/backend/compare/v1.7.0...v1.8.0) (2026-04-22)

### Features

* **agreement:** Add consumer-city and data consumer logo to contract-revision-response. ([a47ed1c](https://github.com/agridata-ch/backend/commit/a47ed1c6e0313046bc3a94b8eb018838619b064d)), closes [DIGIB2-1275](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1275)
* **agreement:** Add contract revisions. ([777f473](https://github.com/agridata-ch/backend/commit/777f473df8cf5216e8f23aac7b620cf147a7757b)), closes [DIGIB2-1275](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1275)
* **agreement:** add lastModifiedDateTime to ConsentRequestFundamentalViewDto ([ea75052](https://github.com/agridata-ch/backend/commit/ea75052267038371e587abaefe2da88aedf9783e))
* **agreement:** Add userId-field to user-info endpoint. ([733d010](https://github.com/agridata-ch/backend/commit/733d01009c1575ab3ebc637788c17d051e1dea7b)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)
* **agreement:** Allow consumer to sign contract. ([055a462](https://github.com/agridata-ch/backend/commit/055a462e10cf8f8919ec207fbfa4862fec0e91d7)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)
* **agreement:** Allow consumers to release datarequests after signing. ([093fea0](https://github.com/agridata-ch/backend/commit/093fea0723f97d1fa83c7eba86ab40c77be94aa1)), closes [DIGIB2-308](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-308)
* **agreement:** Implement sending out otp via sms ([136e02f](https://github.com/agridata-ch/backend/commit/136e02fc66afb01dbb156eeefffb73b99f9e1b62)), closes [DIGIB2-1276](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1276)
* **agreement:** improve maintainability of DataRequestStateService ([e3a6ad5](https://github.com/agridata-ch/backend/commit/e3a6ad56fe25f502ba74ce13042e5e5c90a1bf4d)), closes [DIGIB2-308](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-308)
* **authentication:** add producer_role for equid owners and security arch unit tests ([179807d](https://github.com/agridata-ch/backend/commit/179807da3b1913a7872e07972aeda115f1386521)), closes [DIGIB2-1267](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1267)
* **contract:** Allow provider to access datarequests in the state TO_BE_ACTIVATED. ([6e25eeb](https://github.com/agridata-ch/backend/commit/6e25eeb762423f57cd9b4fc6a432868c5f9ec021)), closes [DIGIB2-1204](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1204)
* **contract:** exclude unnecessary xml-apis dependency ([4f7595a](https://github.com/agridata-ch/backend/commit/4f7595a265026992b531315a54a4c01dfc968a60)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)
* **contract:** get data provider information from uid register ([e65656d](https://github.com/agridata-ch/backend/commit/e65656d6a2e35c263c920edc3637ede146fa8dd8)), closes [DIGIB2-395](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-395)
* **contract:** implement contract PDF generation ([f1b4237](https://github.com/agridata-ch/backend/commit/f1b42372c078ac8ed7762e48a7058d5be42ff81b)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)
* **contract:** implement provider signatures ([4738f62](https://github.com/agridata-ch/backend/commit/4738f6230798ca6c45d4ac56ac710e5baa67a38a)), closes [DIGIB2-1204](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1204)
* **contract:** Make adjustments to contract revision PDF ([31107a3](https://github.com/agridata-ch/backend/commit/31107a376769f4a2278dcde03bd485f8dc2da1fd)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)
* **contracts:** initial connection to bit signature api ([0a893b4](https://github.com/agridata-ch/backend/commit/0a893b4fdf5f655454084fba07047aaa95baa8dd)), closes [DIGIB2-1330](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1330)
* **contracts:** make signing process async and add poll endpoint ([d215375](https://github.com/agridata-ch/backend/commit/d21537515f76a26607aa7de675ddc9bf3f6238dd)), closes [DIGIB2-1330](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1330)
* **data-products:** add and update identitas data products ([8d8fa82](https://github.com/agridata-ch/backend/commit/8d8fa824bd8e85a84bce48ff07b0b092c75c90f6)), closes [DIGIB2-1335](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1335)
* **data-request:** add edit of redirect uri regex as admin ([150dfa7](https://github.com/agridata-ch/backend/commit/150dfa7c7fa54dd5a425a452f983995e3b8f1c07)), closes [DIGIB2-1252](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1252)
* **data-transfer:** add change detection endpoint returning modified producers since a given date ([6d1de16](https://github.com/agridata-ch/backend/commit/6d1de16f400dbf441f15d54d8e9d92e0a937df51)), closes [DIGIB2-861](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-861)
* **deps:** update dependencies ([b360a04](https://github.com/agridata-ch/backend/commit/b360a0478986572e0d3bc83993ca432307efcbfc)), closes [DIGIB2-1289](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1289)
* **error-message:** improve error messages missing uid person ([1942bd6](https://github.com/agridata-ch/backend/commit/1942bd669bb9e44b752ddb73da691f4616e5c0c5)), closes [DIGIB2-1249](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1249)
* **job:** add scheduled job userId support for auditing in system jobs ([d6be1b8](https://github.com/agridata-ch/backend/commit/d6be1b86caaedac10002830b36c31e307c0fc9ba)), closes [DIGIB2-1261](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1261)
* **products:** add identitas data products and test requests ([147b8d6](https://github.com/agridata-ch/backend/commit/147b8d654a1246bd82fbd065c442d004cc152080)), closes [DIGIB2-1295](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1295)

### Bug Fixes

* **agreement:** Change phoneNumber anonymization, retrieve signatureName through .getString() and improve tests. ([a8131fe](https://github.com/agridata-ch/backend/commit/a8131fe348b5e997e95f456602f9edfe4513d8d5)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)
* **agreement:** Concatenate first- and familyName for signature name. ([aca90d5](https://github.com/agridata-ch/backend/commit/aca90d53bc16ad17d9ae5cdb74b90df0fd5c332b)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)
* **archunit:** fix archunit rules ([9903a2a](https://github.com/agridata-ch/backend/commit/9903a2aaeb6342126dc130f9be20b6a66be633cf))
* **cicd:** increase timeout of owasp dependency check due to update duration ([d23bf39](https://github.com/agridata-ch/backend/commit/d23bf39e73148f7620d4c852f0a3c5c4660f6ecf))
* **deps:** update dependencies to fix vulnerabilities ([2b56fc7](https://github.com/agridata-ch/backend/commit/2b56fc73cd2591bff648e4703807b59bea170260))
* **image:** updated docker image ([b5cb51d](https://github.com/agridata-ch/backend/commit/b5cb51d2500eb3d2712181e71dcca75f39598c23))
* **openapi:** filter unused schemas from API subset documents ([bd56d79](https://github.com/agridata-ch/backend/commit/bd56d7972f6ee709c9db59868df60bf74ed34e2d))
* **owasp:** suppress owasp vulnerability until next quarkus update ([8485625](https://github.com/agridata-ch/backend/commit/8485625dd5b2f040757ab918d790fecae97a084c))
* **security:** update zlib to fix vulnerability ([d4f2b57](https://github.com/agridata-ch/backend/commit/d4f2b572d23c0752d4a0056825512deda41e9cd8)), closes [DIGIB2-1277](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1277)
* **sonarqube:** address minor sonarqube issues ([1fce2f5](https://github.com/agridata-ch/backend/commit/1fce2f5544edd17c0527f20842e7d6eed7f898c9)), closes [DIGIB2-1312](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1312)
* **trivy:** patch vulnerability in image ([87454a4](https://github.com/agridata-ch/backend/commit/87454a43c2f4d743610eefb554d20ab2993da519))

## [1.8.0-rc.33](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.32...v1.8.0-rc.33) (2026-04-22)

### Bug Fixes

* **image:** updated docker image ([b5cb51d](https://github.com/agridata-ch/backend/commit/b5cb51d2500eb3d2712181e71dcca75f39598c23))

## [1.8.0-rc.32](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.31...v1.8.0-rc.32) (2026-04-21)

### Bug Fixes

* **sonarqube:** address minor sonarqube issues ([1fce2f5](https://github.com/agridata-ch/backend/commit/1fce2f5544edd17c0527f20842e7d6eed7f898c9)), closes [DIGIB2-1312](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1312)

## [1.8.0-rc.31](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.30...v1.8.0-rc.31) (2026-04-21)

### Features

* **contract:** Make adjustments to contract revision PDF ([31107a3](https://github.com/agridata-ch/backend/commit/31107a376769f4a2278dcde03bd485f8dc2da1fd)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)

## [1.8.0-rc.30](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.29...v1.8.0-rc.30) (2026-04-21)

### Features

* **contract:** exclude unnecessary xml-apis dependency ([4f7595a](https://github.com/agridata-ch/backend/commit/4f7595a265026992b531315a54a4c01dfc968a60)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)

## [1.8.0-rc.29](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.28...v1.8.0-rc.29) (2026-04-20)

### Features

* **contract:** implement contract PDF generation ([f1b4237](https://github.com/agridata-ch/backend/commit/f1b42372c078ac8ed7762e48a7058d5be42ff81b)), closes [DIGIB2-305](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-305)

## [1.8.0-rc.28](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.27...v1.8.0-rc.28) (2026-04-17)

### Features

* **data-products:** add and update identitas data products ([8d8fa82](https://github.com/agridata-ch/backend/commit/8d8fa824bd8e85a84bce48ff07b0b092c75c90f6)), closes [DIGIB2-1335](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1335)

## [1.8.0-rc.27](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.26...v1.8.0-rc.27) (2026-04-16)

### Features

* **error-message:** improve error messages missing uid person ([1942bd6](https://github.com/agridata-ch/backend/commit/1942bd669bb9e44b752ddb73da691f4616e5c0c5)), closes [DIGIB2-1249](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1249)

## [1.8.0-rc.26](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.25...v1.8.0-rc.26) (2026-04-16)

### Features

* **contracts:** make signing process async and add poll endpoint ([d215375](https://github.com/agridata-ch/backend/commit/d21537515f76a26607aa7de675ddc9bf3f6238dd)), closes [DIGIB2-1330](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1330)

### Bug Fixes

* **cicd:** increase timeout of owasp dependency check due to update duration ([d23bf39](https://github.com/agridata-ch/backend/commit/d23bf39e73148f7620d4c852f0a3c5c4660f6ecf))

## [1.8.0-rc.25](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.24...v1.8.0-rc.25) (2026-04-15)

### Bug Fixes

* **owasp:** suppress owasp vulnerability until next quarkus update ([8485625](https://github.com/agridata-ch/backend/commit/8485625dd5b2f040757ab918d790fecae97a084c))
* **trivy:** patch vulnerability in image ([87454a4](https://github.com/agridata-ch/backend/commit/87454a43c2f4d743610eefb554d20ab2993da519))

## [1.8.0-rc.24](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.23...v1.8.0-rc.24) (2026-04-14)

### Features

* **contract:** Allow provider to access datarequests in the state TO_BE_ACTIVATED. ([6e25eeb](https://github.com/agridata-ch/backend/commit/6e25eeb762423f57cd9b4fc6a432868c5f9ec021)), closes [DIGIB2-1204](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1204)

## [1.8.0-rc.23](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.22...v1.8.0-rc.23) (2026-04-13)

### Features

* **contract:** implement provider signatures ([4738f62](https://github.com/agridata-ch/backend/commit/4738f6230798ca6c45d4ac56ac710e5baa67a38a)), closes [DIGIB2-1204](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1204)

## [1.8.0-rc.22](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.21...v1.8.0-rc.22) (2026-04-13)

### Features

* **contracts:** initial connection to bit signature api ([0a893b4](https://github.com/agridata-ch/backend/commit/0a893b4fdf5f655454084fba07047aaa95baa8dd)), closes [DIGIB2-1330](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1330)

## [1.8.0-rc.21](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.20...v1.8.0-rc.21) (2026-04-13)

### Bug Fixes

* **deps:** update dependencies to fix vulnerabilities ([2b56fc7](https://github.com/agridata-ch/backend/commit/2b56fc73cd2591bff648e4703807b59bea170260))

## [1.8.0-rc.20](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.19...v1.8.0-rc.20) (2026-04-08)

### Features

* **agreement:** Implement sending out otp via sms ([136e02f](https://github.com/agridata-ch/backend/commit/136e02fc66afb01dbb156eeefffb73b99f9e1b62)), closes [DIGIB2-1276](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1276)

## [1.8.0-rc.19](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.18...v1.8.0-rc.19) (2026-04-08)

### Features

* **contract:** get data provider information from uid register ([e65656d](https://github.com/agridata-ch/backend/commit/e65656d6a2e35c263c920edc3637ede146fa8dd8)), closes [DIGIB2-395](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-395)

## [1.8.0-rc.18](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.17...v1.8.0-rc.18) (2026-04-02)

### Bug Fixes

* **archunit:** fix archunit rules ([9903a2a](https://github.com/agridata-ch/backend/commit/9903a2aaeb6342126dc130f9be20b6a66be633cf))

## [1.8.0-rc.17](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.16...v1.8.0-rc.17) (2026-04-02)

### Bug Fixes

* **openapi:** filter unused schemas from API subset documents ([bd56d79](https://github.com/agridata-ch/backend/commit/bd56d7972f6ee709c9db59868df60bf74ed34e2d))

## [1.8.0-rc.16](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.15...v1.8.0-rc.16) (2026-04-02)

### Features

* **job:** add scheduled job userId support for auditing in system jobs ([d6be1b8](https://github.com/agridata-ch/backend/commit/d6be1b86caaedac10002830b36c31e307c0fc9ba)), closes [DIGIB2-1261](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1261)

## [1.8.0-rc.15](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.14...v1.8.0-rc.15) (2026-04-01)

### Features

* **agreement:** improve maintainability of DataRequestStateService ([e3a6ad5](https://github.com/agridata-ch/backend/commit/e3a6ad56fe25f502ba74ce13042e5e5c90a1bf4d)), closes [DIGIB2-308](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-308)

## [1.8.0-rc.14](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.13...v1.8.0-rc.14) (2026-04-01)

### Features

* **agreement:** Allow consumers to release datarequests after signing. ([093fea0](https://github.com/agridata-ch/backend/commit/093fea0723f97d1fa83c7eba86ab40c77be94aa1)), closes [DIGIB2-308](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-308)

## [1.8.0-rc.13](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.12...v1.8.0-rc.13) (2026-04-01)

### Features

* **agreement:** add lastModifiedDateTime to ConsentRequestFundamentalViewDto ([ea75052](https://github.com/agridata-ch/backend/commit/ea75052267038371e587abaefe2da88aedf9783e))

## [1.8.0-rc.12](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.11...v1.8.0-rc.12) (2026-03-27)

### Features

* **deps:** update dependencies ([b360a04](https://github.com/agridata-ch/backend/commit/b360a0478986572e0d3bc83993ca432307efcbfc)), closes [DIGIB2-1289](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1289)

## [1.8.0-rc.11](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.10...v1.8.0-rc.11) (2026-03-27)

### Features

* **products:** add identitas data products and test requests ([147b8d6](https://github.com/agridata-ch/backend/commit/147b8d654a1246bd82fbd065c442d004cc152080)), closes [DIGIB2-1295](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1295)

## [1.8.0-rc.10](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.9...v1.8.0-rc.10) (2026-03-26)

### Features

* **data-transfer:** add change detection endpoint returning modified producers since a given date ([6d1de16](https://github.com/agridata-ch/backend/commit/6d1de16f400dbf441f15d54d8e9d92e0a937df51)), closes [DIGIB2-861](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-861)

## [1.8.0-rc.9](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.8...v1.8.0-rc.9) (2026-03-25)

### Bug Fixes

* **agreement:** Change phoneNumber anonymization, retrieve signatureName through .getString() and improve tests. ([a8131fe](https://github.com/agridata-ch/backend/commit/a8131fe348b5e997e95f456602f9edfe4513d8d5)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)

## [1.8.0-rc.8](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.7...v1.8.0-rc.8) (2026-03-24)

### Bug Fixes

* **agreement:** Concatenate first- and familyName for signature name. ([aca90d5](https://github.com/agridata-ch/backend/commit/aca90d53bc16ad17d9ae5cdb74b90df0fd5c332b)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)

## [1.8.0-rc.7](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.6...v1.8.0-rc.7) (2026-03-24)

### Features

* **agreement:** Add userId-field to user-info endpoint. ([733d010](https://github.com/agridata-ch/backend/commit/733d01009c1575ab3ebc637788c17d051e1dea7b)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)

## [1.8.0-rc.6](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.5...v1.8.0-rc.6) (2026-03-20)

### Features

* **authentication:** add producer_role for equid owners and security arch unit tests ([179807d](https://github.com/agridata-ch/backend/commit/179807da3b1913a7872e07972aeda115f1386521)), closes [DIGIB2-1267](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1267)

## [1.8.0-rc.5](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.4...v1.8.0-rc.5) (2026-03-20)

### Features

* **agreement:** Allow consumer to sign contract. ([055a462](https://github.com/agridata-ch/backend/commit/055a462e10cf8f8919ec207fbfa4862fec0e91d7)), closes [DIGIB2-306](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-306)

## [1.8.0-rc.4](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.3...v1.8.0-rc.4) (2026-03-18)

### Features

* **data-request:** add edit of redirect uri regex as admin ([150dfa7](https://github.com/agridata-ch/backend/commit/150dfa7c7fa54dd5a425a452f983995e3b8f1c07)), closes [DIGIB2-1252](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1252)

## [1.8.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.2...v1.8.0-rc.3) (2026-03-17)

### Features

* **agreement:** Add consumer-city and data consumer logo to contract-revision-response. ([a47ed1c](https://github.com/agridata-ch/backend/commit/a47ed1c6e0313046bc3a94b8eb018838619b064d)), closes [DIGIB2-1275](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1275)

## [1.8.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.8.0-rc.1...v1.8.0-rc.2) (2026-03-17)

### Bug Fixes

* **security:** update zlib to fix vulnerability ([d4f2b57](https://github.com/agridata-ch/backend/commit/d4f2b572d23c0752d4a0056825512deda41e9cd8)), closes [DIGIB2-1277](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1277)

## [1.8.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.7.0...v1.8.0-rc.1) (2026-03-17)

### Features

* **agreement:** Add contract revisions. ([777f473](https://github.com/agridata-ch/backend/commit/777f473df8cf5216e8f23aac7b620cf147a7757b)), closes [DIGIB2-1275](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1275)

## [1.7.0](https://github.com/agridata-ch/backend/compare/v1.6.0...v1.7.0) (2026-03-13)

### Features

* **agreement:** Allow the deletion of data requests. ([c9a2a7e](https://github.com/agridata-ch/backend/commit/c9a2a7e07e79c3bb5a1b7bb406f7a2707597c430)), closes [DIGIB2-1188](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1188)
* **agreement:** Allow updating data requests with preexisting deprecated data products. ([db3fbdb](https://github.com/agridata-ch/backend/commit/db3fbdb591dcdc5f7a695f7f006c1d8b8f4cc832)), closes [DIGIB2-1200](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1200)
* **agreement:** Invalidate consent-confirmations when farm changes ownership. ([0b64658](https://github.com/agridata-ch/backend/commit/0b646588121a02b02af17bef85f486f9d09b8ec4)), closes [DIGIB2-1205](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1205)
* **agreement:** restrict data request creation based on requests in draft status ([371fc59](https://github.com/agridata-ch/backend/commit/371fc597d2483b2e67de2fba36d044aa436c7291)), closes [DIGIB2-1187](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1187)
* **auditing:** Audit consent request terminations. ([a9ab505](https://github.com/agridata-ch/backend/commit/a9ab505c8a6e67c9979157c58ac9718f8f909d12)), closes [DIGIB2-1265](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1265)
* **consent-requests:** add endpoint for data provider and adjust consent logic in data transfer ([137484c](https://github.com/agridata-ch/backend/commit/137484cb63b1d395f35240b3f6d5135559dcc50b)), closes [DIGIB2-1137](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1137)
* **data-transfer:** implementation of BurBasedPostValidationFlow for tvd data product ([0d97dca](https://github.com/agridata-ch/backend/commit/0d97dca64d6f71dad4c61a61e7e530c05b4f0aef)), closes [DIGIB2-1178](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1178)
* **data-transfer:** implementation of UidBasedPostValidationFlow for first tvd data product ([6d3024d](https://github.com/agridata-ch/backend/commit/6d3024da9b2c07ec8c2e6500cbdea360ae5943e3))
* **data-transfer:** implementation of UnboundPostValidationFlow for tvd eartag data product ([8ac6c03](https://github.com/agridata-ch/backend/commit/8ac6c03e5c4244efb0f1df8dedb49eebd54de720)), closes [DIGIB2-1179](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1179)
* **openapi:** remove unneeded openapi example ([03c15f2](https://github.com/agridata-ch/backend/commit/03c15f2480d83239bfb12def206efe9470960b42)), closes [DIGIB2-1137](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1137)
* **product:** Add deprecated_since field to data_product table. ([c3f73c9](https://github.com/agridata-ch/backend/commit/c3f73c9bc9e982f71c071f51c7708ee94dd0ffa4)), closes [DIGIB2-1200](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1200)
* **testdata:** Support SQL file upload and restrict execution to non-prod profiles ([fb456a0](https://github.com/agridata-ch/backend/commit/fb456a03c9028e565179d7860c5db3a73a7dd36e)), closes [DIGIB2-1234](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1234)
* **user:** add consent request tour intro flag to user preferences. ([d391b31](https://github.com/agridata-ch/backend/commit/d391b3135e490651fd1e9cbe3267036b71c73e2c)), closes [DIGIB2-524](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-524)

### Bug Fixes

* **test-data:** remove no more needed test data and minor refactoring in data-transfer v2 ([13909fc](https://github.com/agridata-ch/backend/commit/13909fc2e739b0e8efd591e85bbf4e743486b9a1)), closes [DIGIB2-1246](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1246)

## [1.7.0-rc.13](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.12...v1.7.0-rc.13) (2026-03-13)

### Features

* **openapi:** remove unneeded openapi example ([03c15f2](https://github.com/agridata-ch/backend/commit/03c15f2480d83239bfb12def206efe9470960b42)), closes [DIGIB2-1137](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1137)

## [1.7.0-rc.12](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.11...v1.7.0-rc.12) (2026-03-11)

### Features

* **auditing:** Audit consent request terminations. ([a9ab505](https://github.com/agridata-ch/backend/commit/a9ab505c8a6e67c9979157c58ac9718f8f909d12)), closes [DIGIB2-1265](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1265)

## [1.7.0-rc.11](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.10...v1.7.0-rc.11) (2026-03-11)

### Features

* **consent-requests:** add endpoint for data provider and adjust consent logic in data transfer ([137484c](https://github.com/agridata-ch/backend/commit/137484cb63b1d395f35240b3f6d5135559dcc50b)), closes [DIGIB2-1137](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1137)

## [1.7.0-rc.10](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.9...v1.7.0-rc.10) (2026-03-11)

### Features

* **agreement:** Allow updating data requests with preexisting deprecated data products. ([db3fbdb](https://github.com/agridata-ch/backend/commit/db3fbdb591dcdc5f7a695f7f006c1d8b8f4cc832)), closes [DIGIB2-1200](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1200)

## [1.7.0-rc.9](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.8...v1.7.0-rc.9) (2026-03-10)

### Features

* **product:** Add deprecated_since field to data_product table. ([c3f73c9](https://github.com/agridata-ch/backend/commit/c3f73c9bc9e982f71c071f51c7708ee94dd0ffa4)), closes [DIGIB2-1200](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1200)

## [1.7.0-rc.8](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.7...v1.7.0-rc.8) (2026-03-05)

### Features

* **agreement:** Allow the deletion of data requests. ([c9a2a7e](https://github.com/agridata-ch/backend/commit/c9a2a7e07e79c3bb5a1b7bb406f7a2707597c430)), closes [DIGIB2-1188](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1188)

## [1.7.0-rc.7](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.6...v1.7.0-rc.7) (2026-03-05)

### Features

* **agreement:** restrict data request creation based on requests in draft status ([371fc59](https://github.com/agridata-ch/backend/commit/371fc597d2483b2e67de2fba36d044aa436c7291)), closes [DIGIB2-1187](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1187)

## [1.7.0-rc.6](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.5...v1.7.0-rc.6) (2026-03-03)

### Features

* **user:** add consent request tour intro flag to user preferences. ([d391b31](https://github.com/agridata-ch/backend/commit/d391b3135e490651fd1e9cbe3267036b71c73e2c)), closes [DIGIB2-524](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-524)

## [1.7.0-rc.5](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.4...v1.7.0-rc.5) (2026-03-02)

### Features

* **agreement:** Invalidate consent-confirmations when farm changes ownership. ([0b64658](https://github.com/agridata-ch/backend/commit/0b646588121a02b02af17bef85f486f9d09b8ec4)), closes [DIGIB2-1205](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1205)

## [1.7.0-rc.4](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.3...v1.7.0-rc.4) (2026-02-27)

### Features

* **data-transfer:** implementation of UnboundPostValidationFlow for tvd eartag data product ([8ac6c03](https://github.com/agridata-ch/backend/commit/8ac6c03e5c4244efb0f1df8dedb49eebd54de720)), closes [DIGIB2-1179](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1179)

## [1.7.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.2...v1.7.0-rc.3) (2026-02-27)

### Features

* **data-transfer:** implementation of BurBasedPostValidationFlow for tvd data product ([0d97dca](https://github.com/agridata-ch/backend/commit/0d97dca64d6f71dad4c61a61e7e530c05b4f0aef)), closes [DIGIB2-1178](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1178)

## [1.7.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.7.0-rc.1...v1.7.0-rc.2) (2026-02-25)

### Features

* **data-transfer:** implementation of UidBasedPostValidationFlow for first tvd data product ([6d3024d](https://github.com/agridata-ch/backend/commit/6d3024da9b2c07ec8c2e6500cbdea360ae5943e3))

## [1.7.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.6.1-rc.1...v1.7.0-rc.1) (2026-02-24)

### Features

* **testdata:** Support SQL file upload and restrict execution to non-prod profiles ([fb456a0](https://github.com/agridata-ch/backend/commit/fb456a03c9028e565179d7860c5db3a73a7dd36e)), closes [DIGIB2-1234](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1234)

## [1.6.1-rc.1](https://github.com/agridata-ch/backend/compare/v1.6.0...v1.6.1-rc.1) (2026-02-23)

### Bug Fixes

* **test-data:** remove no more needed test data and minor refactoring in data-transfer v2 ([13909fc](https://github.com/agridata-ch/backend/commit/13909fc2e739b0e8efd591e85bbf4e743486b9a1)), closes [DIGIB2-1246](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1246)

## [1.6.0](https://github.com/agridata-ch/backend/compare/v1.5.0...v1.6.0) (2026-02-19)

### Features

* **agreement:** Add data_producer_bur column to consent_request and add consent-request-aggregation endpoint. ([e96408e](https://github.com/agridata-ch/backend/commit/e96408ec9f75903ec2dd067b85d2fce70fe15dfd)), closes [DIGIB2-1206](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1206)
* **agreement:** Add datasource-field to data-request endpoint. ([4e72e2c](https://github.com/agridata-ch/backend/commit/4e72e2c0bd59fa53bc41aef1f2af98bd1b9c5a3a)), closes [DIGIB2-466](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-466)
* **agreement:** Allow providers to access data requests addressed to them ([1cbd6b4](https://github.com/agridata-ch/backend/commit/1cbd6b4e579206e1edac78a649bf39426e008179)), closes [DIGIB2-466](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-466)
* **audit:** Introduce audit-logging of submission, rejection, approval, activation and withdrawal of data requests. ([3dda6bb](https://github.com/agridata-ch/backend/commit/3dda6bbb5cc4085986396e4cb41182bc03c72a44)), closes [DIGIB2-337](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-337)
* **data-transfer:** implementation of first data transfer workflow ([d92b3ec](https://github.com/agridata-ch/backend/commit/d92b3ec14c66132c0cf83aeafb92780feb3a5f70))
* **deps:** update java to v25 and multiple dependencies ([0de3b77](https://github.com/agridata-ch/backend/commit/0de3b7772cbd096154f5217be50c6580db074d0f)), closes [DIGIB2-1233](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1233)
* **docs:** add publiccode.yml ([69b98c7](https://github.com/agridata-ch/backend/commit/69b98c7a24f603ba54941db157fa724ab40f4794))
* **env:** adds new agridata-testing environment ([20b2986](https://github.com/agridata-ch/backend/commit/20b2986b882b3dea272d492003df504b29d3b94f)), closes [DIGIB2-1213](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1213)
* **openapi:** add openapi subsets for different stakeholders ([a9982e5](https://github.com/agridata-ch/backend/commit/a9982e541781b6fb41657fd68379e257132b528b))
* **product:** Adjust DB-Model to include Data Provider and Data Source System. ([4949523](https://github.com/agridata-ch/backend/commit/4949523445ba13bc8ebcfa06091b3af11efc5e68)), closes [DIGIB2-979](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-979)

### Bug Fixes

* **deps:** update image to fix vulnerability ([7346ffa](https://github.com/agridata-ch/backend/commit/7346ffa884ed0bb82778dd67bce366f5eef94e21))
* **security:** Make sure, PreSecurityLogFilter does no longer log the body of image uploads. ([4ce7b2e](https://github.com/agridata-ch/backend/commit/4ce7b2e715dbdb69c9318ab3d4c5cc5fff0220a4)), closes [DIGIB2-1198](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1198)
* **test-users:** sync local test users with dev users ([bf318be](https://github.com/agridata-ch/backend/commit/bf318be52fbdaa73342ebd69b417cfffaf22864e))

## [1.6.0-rc.10](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.9...v1.6.0-rc.10) (2026-02-18)

### Bug Fixes

* **test-users:** sync local test users with dev users ([bf318be](https://github.com/agridata-ch/backend/commit/bf318be52fbdaa73342ebd69b417cfffaf22864e))

## [1.6.0-rc.9](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.8...v1.6.0-rc.9) (2026-02-18)

### Features

* **agreement:** Add datasource-field to data-request endpoint. ([4e72e2c](https://github.com/agridata-ch/backend/commit/4e72e2c0bd59fa53bc41aef1f2af98bd1b9c5a3a)), closes [DIGIB2-466](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-466)

## [1.6.0-rc.8](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.7...v1.6.0-rc.8) (2026-02-17)

### Features

* **openapi:** add openapi subsets for different stakeholders ([a9982e5](https://github.com/agridata-ch/backend/commit/a9982e541781b6fb41657fd68379e257132b528b))

## [1.6.0-rc.7](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.6...v1.6.0-rc.7) (2026-02-17)

### Features

* **agreement:** Allow providers to access data requests addressed to them ([1cbd6b4](https://github.com/agridata-ch/backend/commit/1cbd6b4e579206e1edac78a649bf39426e008179)), closes [DIGIB2-466](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-466)

## [1.6.0-rc.6](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.5...v1.6.0-rc.6) (2026-02-12)

### Features

* **deps:** update java to v25 and multiple dependencies ([0de3b77](https://github.com/agridata-ch/backend/commit/0de3b7772cbd096154f5217be50c6580db074d0f)), closes [DIGIB2-1233](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1233)

## [1.6.0-rc.5](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.4...v1.6.0-rc.5) (2026-02-09)

### Features

* **product:** Adjust DB-Model to include Data Provider and Data Source System. ([4949523](https://github.com/agridata-ch/backend/commit/4949523445ba13bc8ebcfa06091b3af11efc5e68)), closes [DIGIB2-979](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-979)

## [1.6.0-rc.4](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.3...v1.6.0-rc.4) (2026-02-09)

### Features

* **agreement:** Add data_producer_bur column to consent_request and add consent-request-aggregation endpoint. ([e96408e](https://github.com/agridata-ch/backend/commit/e96408ec9f75903ec2dd067b85d2fce70fe15dfd)), closes [DIGIB2-1206](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1206)
* **data-transfer:** implementation of first data transfer workflow ([d92b3ec](https://github.com/agridata-ch/backend/commit/d92b3ec14c66132c0cf83aeafb92780feb3a5f70))

## [1.6.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.2...v1.6.0-rc.3) (2026-02-06)

### Features

* **env:** adds new agridata-testing environment ([20b2986](https://github.com/agridata-ch/backend/commit/20b2986b882b3dea272d492003df504b29d3b94f)), closes [DIGIB2-1213](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1213)

## [1.6.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.6.0-rc.1...v1.6.0-rc.2) (2026-02-02)

### Bug Fixes

* **security:** Make sure, PreSecurityLogFilter does no longer log the body of image uploads. ([4ce7b2e](https://github.com/agridata-ch/backend/commit/4ce7b2e715dbdb69c9318ab3d4c5cc5fff0220a4)), closes [DIGIB2-1198](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1198)

## [1.6.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.5.1-rc.1...v1.6.0-rc.1) (2026-02-02)

### Features

* **docs:** add publiccode.yml ([69b98c7](https://github.com/agridata-ch/backend/commit/69b98c7a24f603ba54941db157fa724ab40f4794))

## [1.5.1-rc.1](https://github.com/agridata-ch/backend/compare/v1.5.0...v1.5.1-rc.1) (2026-02-02)

### Features

* **audit:** Introduce audit-logging of submission, rejection, approval, activation and withdrawal of data requests. ([3dda6bb](https://github.com/agridata-ch/backend/commit/3dda6bbb5cc4085986396e4cb41182bc03c72a44)), closes [DIGIB2-337](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-337)

### Bug Fixes

* **deps:** update image to fix vulnerability ([7346ffa](https://github.com/agridata-ch/backend/commit/7346ffa884ed0bb82778dd67bce366f5eef94e21))

## [1.5.0](https://github.com/agridata-ch/backend/compare/v1.4.2...v1.5.0) (2026-01-23)

### Features

* **admin:** Only return non-draft datarequests for Admin calling getDataRequest and getDataRequests endpoint. ([40b24a4](https://github.com/agridata-ch/backend/commit/40b24a42e29d464948368c2fef7535e2fb856d4c)), closes [DIGIB2-302](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-302)
* **api:** refactor api paths to align with REST principles ([d336d65](https://github.com/agridata-ch/backend/commit/d336d65ce7b49062ed6b84b0eb85bb8f7083c43b)), closes [DIGIB2-1094](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1094)
* **deps:** Require approval for renovate-bot PRs and update multiple dependencies: ([60e43ce](https://github.com/agridata-ch/backend/commit/60e43ce1af61ed776c81582cfa221e32fa1dda3c)), closes [DIGIB2-1164](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1164)
* **deps:** Updating multiple dependencies: ([7d3a0c4](https://github.com/agridata-ch/backend/commit/7d3a0c48d9d95766bbb7a176b1134c1c9ac81382)), closes [DIGIB2-1164](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1164)
* **equid-owners:** add equid owner uids in dropdown and ensure impersonation can handle this ([13659b8](https://github.com/agridata-ch/backend/commit/13659b820f01f7a67f3484f21bbed7be37b7a8a5)), closes [DIGIB2-1094](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1094)
* **test-users:** add new test users and remove json rewrite ([e601a7c](https://github.com/agridata-ch/backend/commit/e601a7c89f63932e8ce68039a186f9295a9c6eda)), closes [DIGIB2-1075](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1075)

### Bug Fixes

* **dataRequest:** Prevent submission of data requests with non-existent data products ([140ab24](https://github.com/agridata-ch/backend/commit/140ab2434220e14de77f658efca605713ba28614)), closes [DIGIB2-1052](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1052)
* **dataRequest:** Prevent submission of data requests with values that don't adhere to size constraints ([1cf3636](https://github.com/agridata-ch/backend/commit/1cf363607befe27618a6cb47bc5345b0ae02502c)), closes [DIGIB2-1051](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1051)
* **security:** Improve security for logo upload. ([13ac7fc](https://github.com/agridata-ch/backend/commit/13ac7fcb6b0bb717cb6a4aff6dd5947ab271884a)), closes [DIGIB2-556](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-556)

## [1.5.0-rc.7](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.6...v1.5.0-rc.7) (2026-01-21)

### Features

* **api:** refactor api paths to align with REST principles ([d336d65](https://github.com/agridata-ch/backend/commit/d336d65ce7b49062ed6b84b0eb85bb8f7083c43b)), closes [DIGIB2-1094](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1094)

## [1.5.0-rc.6](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.5...v1.5.0-rc.6) (2026-01-20)

### Features

* **equid-owners:** add equid owner uids in dropdown and ensure impersonation can handle this ([13659b8](https://github.com/agridata-ch/backend/commit/13659b820f01f7a67f3484f21bbed7be37b7a8a5)), closes [DIGIB2-1094](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1094)

## [1.5.0-rc.5](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.4...v1.5.0-rc.5) (2026-01-19)

### Bug Fixes

* **security:** Improve security for logo upload. ([13ac7fc](https://github.com/agridata-ch/backend/commit/13ac7fcb6b0bb717cb6a4aff6dd5947ab271884a)), closes [DIGIB2-556](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-556)

## [1.5.0-rc.4](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.3...v1.5.0-rc.4) (2026-01-08)

### Features

* **admin:** Only return non-draft datarequests for Admin calling getDataRequest and getDataRequests endpoint. ([40b24a4](https://github.com/agridata-ch/backend/commit/40b24a42e29d464948368c2fef7535e2fb856d4c)), closes [DIGIB2-302](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-302)

## [1.5.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.2...v1.5.0-rc.3) (2025-12-18)

### Features

* **deps:** Require approval for renovate-bot PRs and update multiple dependencies: ([60e43ce](https://github.com/agridata-ch/backend/commit/60e43ce1af61ed776c81582cfa221e32fa1dda3c)), closes [DIGIB2-1164](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1164)

## [1.5.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.5.0-rc.1...v1.5.0-rc.2) (2025-12-18)

### Features

* **deps:** Updating multiple dependencies: ([7d3a0c4](https://github.com/agridata-ch/backend/commit/7d3a0c48d9d95766bbb7a176b1134c1c9ac81382)), closes [DIGIB2-1164](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1164)

## [1.5.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.4.3-rc.2...v1.5.0-rc.1) (2025-12-17)

### Features

* **test-users:** add new test users and remove json rewrite ([e601a7c](https://github.com/agridata-ch/backend/commit/e601a7c89f63932e8ce68039a186f9295a9c6eda)), closes [DIGIB2-1075](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1075)

## [1.4.3-rc.2](https://github.com/agridata-ch/backend/compare/v1.4.3-rc.1...v1.4.3-rc.2) (2025-12-16)

### Bug Fixes

* **dataRequest:** Prevent submission of data requests with non-existent data products ([140ab24](https://github.com/agridata-ch/backend/commit/140ab2434220e14de77f658efca605713ba28614)), closes [DIGIB2-1052](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1052)

## [1.4.3-rc.1](https://github.com/agridata-ch/backend/compare/v1.4.2...v1.4.3-rc.1) (2025-12-11)

### Bug Fixes

* **dataRequest:** Prevent submission of data requests with values that don't adhere to size constraints ([1cf3636](https://github.com/agridata-ch/backend/commit/1cf363607befe27618a6cb47bc5345b0ae02502c)), closes [DIGIB2-1051](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1051)

## [1.4.2](https://github.com/agridata-ch/backend/compare/v1.4.1...v1.4.2) (2025-12-10)

### Bug Fixes

* **image:** change image to alpine linux ([1c343ee](https://github.com/agridata-ch/backend/commit/1c343eed7d0217742b87f3bd8caf83551b540e1f)), closes [DIGIB2-1084](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1084)
* **users:** add all user data when creating a new user ([11d6715](https://github.com/agridata-ch/backend/commit/11d6715e92140305107f34006b4c2891787b23be)), closes [DIGIB2-1063](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1063)

## [1.4.2-rc.2](https://github.com/agridata-ch/backend/compare/v1.4.2-rc.1...v1.4.2-rc.2) (2025-12-10)

### Bug Fixes

* **image:** change image to alpine linux ([1c343ee](https://github.com/agridata-ch/backend/commit/1c343eed7d0217742b87f3bd8caf83551b540e1f)), closes [DIGIB2-1084](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1084)

## [1.4.2-rc.1](https://github.com/agridata-ch/backend/compare/v1.4.1...v1.4.2-rc.1) (2025-12-08)

### Bug Fixes

* **users:** add all user data when creating a new user ([11d6715](https://github.com/agridata-ch/backend/commit/11d6715e92140305107f34006b4c2891787b23be)), closes [DIGIB2-1063](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1063)

## [1.4.1](https://github.com/agridata-ch/backend/compare/v1.4.0...v1.4.1) (2025-12-01)

### Bug Fixes

* **agis:** add refresh-token-time-skew for agis api ([8e3c7e6](https://github.com/agridata-ch/backend/commit/8e3c7e674df9e24a814d56a81149e1b428228d3e)), closes [DIGIB2-1071](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1071)

## [1.4.1-rc.1](https://github.com/agridata-ch/backend/compare/v1.4.0...v1.4.1-rc.1) (2025-12-01)

### Bug Fixes

* **agis:** add refresh-token-time-skew for agis api ([8e3c7e6](https://github.com/agridata-ch/backend/commit/8e3c7e674df9e24a814d56a81149e1b428228d3e)), closes [DIGIB2-1071](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1071)

## [1.4.0](https://github.com/agridata-ch/backend/compare/v1.3.0...v1.4.0) (2025-11-27)

### Features

* **user-preferences:** adds user preferences ([4845e53](https://github.com/agridata-ch/backend/commit/4845e5348e499a8845d9e1d0e5f75003f4893574)), closes [DIGIB2-586](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-586)

### Bug Fixes

* **test-controller:** prevents content type errors in frontend on reset. ([0432fee](https://github.com/agridata-ch/backend/commit/0432feeb6908ae133f477b0d8d0086ff1f4ef724))
* **test-data:** fix test data reset ([9058629](https://github.com/agridata-ch/backend/commit/9058629af897b6ff62e32f4547a38b0b80a27d87)), closes [DIGIB2-1069](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1069)

## [1.4.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.4.0-rc.2...v1.4.0-rc.3) (2025-11-27)

### Bug Fixes

* **test-data:** fix test data reset ([9058629](https://github.com/agridata-ch/backend/commit/9058629af897b6ff62e32f4547a38b0b80a27d87)), closes [DIGIB2-1069](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1069)

## [1.4.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.4.0-rc.1...v1.4.0-rc.2) (2025-11-26)

### Bug Fixes

* **test-controller:** prevents content type errors in frontend on reset. ([0432fee](https://github.com/agridata-ch/backend/commit/0432feeb6908ae133f477b0d8d0086ff1f4ef724))

## [1.4.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.3.0...v1.4.0-rc.1) (2025-11-24)

### Features

* **user-preferences:** adds user preferences ([4845e53](https://github.com/agridata-ch/backend/commit/4845e5348e499a8845d9e1d0e5f75003f4893574)), closes [DIGIB2-586](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-586)

## [1.3.0](https://github.com/agridata-ch/backend/compare/v1.2.1...v1.3.0) (2025-11-19)

### Features

* **consent-request:** adds endpoint to get single consent request ([441741f](https://github.com/agridata-ch/backend/commit/441741f82f38bc56abb4ffec8c211f20aee1a58b)), closes [DIGIB2-1020](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1020)
* **deps:** update actions/setup-node action to v6 ([f6a91f8](https://github.com/agridata-ch/backend/commit/f6a91f8f54bb6804d38532b81c5870fd9230edc9))
* **deps:** update amazoncorretto docker tag to v25 ([21946ec](https://github.com/agridata-ch/backend/commit/21946ec55cfcf429dd40c443f88b3349559fd248))
* **deps:** update dependency node to v24 ([0357c8b](https://github.com/agridata-ch/backend/commit/0357c8bbbdee08822313b210a474f4c7ece9f932))
* **deps:** update renovatebot/github-action action to v44 ([5fdbcdd](https://github.com/agridata-ch/backend/commit/5fdbcdd104b9b7c41e6c0de84c9b55c29d34c889))
* **openapi-gernation:** improves api generation at build time ([235f8c5](https://github.com/agridata-ch/backend/commit/235f8c52790747cc8c5a07fe5953006893609620)), closes [DIGIB2-995](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-995)

## [1.3.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.2.1...v1.3.0-rc.1) (2025-11-17)

### Features

* **consent-request:** adds endpoint to get single consent request ([441741f](https://github.com/agridata-ch/backend/commit/441741f82f38bc56abb4ffec8c211f20aee1a58b)), closes [DIGIB2-1020](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1020)
* **deps:** update actions/setup-node action to v6 ([f6a91f8](https://github.com/agridata-ch/backend/commit/f6a91f8f54bb6804d38532b81c5870fd9230edc9))
* **deps:** update amazoncorretto docker tag to v25 ([21946ec](https://github.com/agridata-ch/backend/commit/21946ec55cfcf429dd40c443f88b3349559fd248))
* **deps:** update dependency node to v24 ([0357c8b](https://github.com/agridata-ch/backend/commit/0357c8bbbdee08822313b210a474f4c7ece9f932))
* **deps:** update renovatebot/github-action action to v44 ([5fdbcdd](https://github.com/agridata-ch/backend/commit/5fdbcdd104b9b7c41e6c0de84c9b55c29d34c889))
* **openapi-gernation:** improves api generation at build time ([235f8c5](https://github.com/agridata-ch/backend/commit/235f8c52790747cc8c5a07fe5953006893609620)), closes [DIGIB2-995](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-995)

## [1.2.1](https://github.com/agridata-ch/backend/compare/v1.2.0...v1.2.1) (2025-11-05)

### Bug Fixes

* **product:** change request template of data product ([6f6fdc3](https://github.com/agridata-ch/backend/commit/6f6fdc3db6035e5c11fa4591898362a3f1abce7d)), closes [DIGIB2-1015](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1015)

## [1.2.1-rc.1](https://github.com/agridata-ch/backend/compare/v1.2.0...v1.2.1-rc.1) (2025-11-05)

### Bug Fixes

* **product:** change request template of data product ([6f6fdc3](https://github.com/agridata-ch/backend/commit/6f6fdc3db6035e5c11fa4591898362a3f1abce7d)), closes [DIGIB2-1015](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1015)

## [1.2.0](https://github.com/agridata-ch/backend/compare/v1.1.0...v1.2.0) (2025-10-31)

### Features

* **products:** improve names and description of data products ([7a4a27b](https://github.com/agridata-ch/backend/commit/7a4a27b9cebeca38d01b6fe1d3e374ff0956b821)), closes [DIGIB2-1004](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1004)

### Bug Fixes

* **logo:** disable logo upload functionality temporarily ([3fdcb30](https://github.com/agridata-ch/backend/commit/3fdcb3058be3d35adca1057e3a3e48e3319f066a))

## [1.2.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.2.0-rc.1...v1.2.0-rc.2) (2025-10-31)

### Bug Fixes

* **logo:** disable logo upload functionality temporarily ([3fdcb30](https://github.com/agridata-ch/backend/commit/3fdcb3058be3d35adca1057e3a3e48e3319f066a))

## [1.2.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.1.0...v1.2.0-rc.1) (2025-10-31)

### Features

* **products:** improve names and description of data products ([7a4a27b](https://github.com/agridata-ch/backend/commit/7a4a27b9cebeca38d01b6fe1d3e374ff0956b821)), closes [DIGIB2-1004](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-1004)

## [1.1.0](https://github.com/agridata-ch/backend/compare/v1.0.1...v1.1.0) (2025-10-29)

### Features

* **agis-logs:** add virtual threads, add json logging, improving agis logs ([785ba90](https://github.com/agridata-ch/backend/commit/785ba907d7f3100c2135ecc213ea4c67480f2336)), closes [DIGIB2-914](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-914)
* **consent-request:** rewrites consent request create endpoint to better conform to Rest standards and allow to create consent requests per uid ([39c1c3e](https://github.com/agridata-ch/backend/commit/39c1c3eb94daae506c65fef7ccd7c27589f20b0c)), closes [DIGIB2-887](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-887)
* **indexes:** add database indexes ([8c4b371](https://github.com/agridata-ch/backend/commit/8c4b371936a0ba1c16ed8d5b26c9b5c42b23df76)), closes [DIGIB2-968](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-968)
* **maintenance:** add maintenance mode to ExceptionEnum ([c805375](https://github.com/agridata-ch/backend/commit/c80537545b041c27dc21c4419ccd05948da30d73)), closes [DIGIB2-261](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-261)

### Bug Fixes

* **duration:** fixes case in rest client duration log / migrates to micrometer (as recommended by Quarkus) ([cf82f54](https://github.com/agridata-ch/backend/commit/cf82f54a25c27d68372efff80fe10661a672c78b)), closes [DIGIB2-958](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-958)
* **flakey-test:** increases test timeout ([f42e046](https://github.com/agridata-ch/backend/commit/f42e046d353d5b21624f749120f777f5e821da4b)), closes [DIGIB2-975](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-975)
* **semantic-release:** triggers semenatic release after cleaning invalid tags ([082b1f1](https://github.com/agridata-ch/backend/commit/082b1f19664f06e40adaf7787da8dd47948c14f2)), closes [DIGIB2-914](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-914)

## [1.1.0-rc.5](https://github.com/agridata-ch/backend/compare/v1.1.0-rc.4...v1.1.0-rc.5) (2025-10-29)

### Bug Fixes

* **flakey-test:** increases test timeout ([f42e046](https://github.com/agridata-ch/backend/commit/f42e046d353d5b21624f749120f777f5e821da4b)), closes [DIGIB2-975](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-975)

## [1.1.0-rc.4](https://github.com/agridata-ch/backend/compare/v1.1.0-rc.3...v1.1.0-rc.4) (2025-10-27)

### Features

* **maintenance:** add maintenance mode to ExceptionEnum ([c805375](https://github.com/agridata-ch/backend/commit/c80537545b041c27dc21c4419ccd05948da30d73)), closes [DIGIB2-261](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-261)

## [1.1.0-rc.3](https://github.com/agridata-ch/backend/compare/v1.1.0-rc.2...v1.1.0-rc.3) (2025-10-23)

### Features

* **consent-request:** rewrites consent request create endpoint to better conform to Rest standards and allow to create consent requests per uid ([39c1c3e](https://github.com/agridata-ch/backend/commit/39c1c3eb94daae506c65fef7ccd7c27589f20b0c)), closes [DIGIB2-887](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-887)

## [1.1.0-rc.2](https://github.com/agridata-ch/backend/compare/v1.1.0-rc.1...v1.1.0-rc.2) (2025-10-23)

### Features

* **indexes:** add database indexes ([8c4b371](https://github.com/agridata-ch/backend/commit/8c4b371936a0ba1c16ed8d5b26c9b5c42b23df76)), closes [DIGIB2-968](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-968)

### Bug Fixes

* **duration:** fixes case in rest client duration log / migrates to micrometer (as recommended by Quarkus) ([cf82f54](https://github.com/agridata-ch/backend/commit/cf82f54a25c27d68372efff80fe10661a672c78b)), closes [DIGIB2-958](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-958)

## [1.1.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.0.1...v1.1.0-rc.1) (2025-10-15)

### Features

* **agis-logs:** add virtual threads, add json logging, improving agis logs ([785ba90](https://github.com/agridata-ch/backend/commit/785ba907d7f3100c2135ecc213ea4c67480f2336)), closes [DIGIB2-914](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-914)

### Bug Fixes

* **semantic-release:** triggers semenatic release after cleaning invalid tags ([082b1f1](https://github.com/agridata-ch/backend/commit/082b1f19664f06e40adaf7787da8dd47948c14f2)), closes [DIGIB2-914](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-914)

## [1.1.0-rc.1](https://github.com/agridata-ch/backend/compare/v1.0.1...v1.1.0-rc.1) (2025-10-15)

### Features

* **agis-logs:** add virtual threads, add json logging, improving agis logs ([785ba90](https://github.com/agridata-ch/backend/commit/785ba907d7f3100c2135ecc213ea4c67480f2336)), closes [DIGIB2-914](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-914)

## [1.0.1](https://github.com/agridata-ch/backend/compare/v1.0.0...v1.0.1) (2025-10-15)

### Bug Fixes

* **changelog:** import changelog ([d871576](https://github.com/agridata-ch/backend/commit/d871576e70f086d1b16d35a89d6569cbcb2cc86c))

## [1.0.1-rc.1](https://github.com/agridata-ch/backend/compare/v1.0.0...v1.0.1-rc.1) (2025-10-15)

### Bug Fixes

* **changelog:** import changelog ([d871576](https://github.com/agridata-ch/backend/commit/d871576e70f086d1b16d35a89d6569cbcb2cc86c))

## 1.0.0 (2025-10-15)

### Features

* **agis:** add certificate, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)
* **agis:** add role for endpoints, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)
* **agis:** adds agis api, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)
* **agis:** change path for certificates, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)
* **agis:** update agis api from 2025-00 to 2025-01
* **api:** add data masking configuration for AGIS api, closes [DIGIB2-485](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-485)
* **api:** fix media type, closes [DIGIB2-338](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-338)
* **api:** update consent request, closes [DIGIB2-435](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-435)
* **audit:** add audit log functionality
* **authentication:** add quarkus dev console as valid redirect uri, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)
* **authentication:** add token validation and protect endpoints, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)
* **authentication:** add validation of audience claim, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)
* **authentication:** enable PKCE flow locally, closes [DIGIB2-462](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-462)
* **authentication:** match local access token with agate token, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)
* **authentication:** replace local dummy agate login ids with real ones for test data reliability, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)
* **authentication:** set local keycloak port, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)
* **cicd:** add manual deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)
* **cicd:** add manual deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)
* **cicd:** add manuel deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)
* **cicd:** add renovate bot, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)
* **cicd:** change configuration of renovate bot, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)
* **config:** remove profiles from application.yml to improve readability
* **configuration:** make swagger available on dev, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)
* **consent-status:** add endpoint for data request consent statuses
* **consent:** add query param validation
* **consent:** enable revert to opened state for consent requests, closes [DIGIB2-447](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-447)
* **cors:** configure cors, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)
* **data-products:** adds data products and data request CRUD, closes [DIGIB2-514](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-514)
* **data-request:** add logo upload and contact information, closes [DIGIB2-403](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-403)
* **data-request:** add submissionDate and humanFriendlyId to data request, closes [DIGIB2-307](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-307)
* **data-request:** add title to data request, closes [DIGIB2-423](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-423)
* **data-request:** adds target group / changes submit to status endpoint, closes [DIGIB2-309](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-309)
* **data-request:** adds uid data to data request, closes [DIGIB2-487](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-487)
* **data-transfer:** add tests, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)
* **data-transfer:** adds agis data transfer module, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)
* **data-transfer:** adds consent request check, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)
* **datarequest-products:** simplify products in dataRequest, closes [DIGIB2-310](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-310)
* **deps:** update dependencies
* **DIGIB2-411:** add checkstyle, owasp scan and trivy scan, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)
* **DIGIB2-411:** first version of data- and consent-requests, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)
* **DIGIB2-639:** enhanced test data, so all cases can be tested & changed tests respectively, closes [DIGIB2-639](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-639)
* **docu:** add code documentation, closes [DIGIB2-710](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-710)
* **error-handling:** requestId not null, closes [DIGIB2-835](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-835)
* **initialize:** adds info endpoint, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)
* **initialize:** adds security configuration, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)
* **license:** update THIRD_PARTY_LICENSES.md
* **log:** add debug logging to json field rewrite
* **log:** add user id to log context
* **migration:** add migratedFromMafDate, closes [DIGIB2-764](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-764)
* **model:** replace table data_consumer with table participant, closes [DIGIB2-423](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-423)
* **open-source:** move license-check to weekly job, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)
* **open-source:** prepare for open-source, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)
* **open-source:** update THIRD_PARTY_LICENSES.md to use github dependency graph, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)
* **pipeline:** trigger release and deployment, closes [DIGIB2-473](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-473)
* **pipeline:** update main pipeline, closes [DIGIB2-898](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-898)
* **products:** add data products for mvp, closes [DIGIB2-694](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-694)
* **refactoring:** split DataRequestService into three separate services
* **renovate:** add dependency dashboard
* **renovate:** exclude patch versions from renovate bot
* **renovate:** update dependencies
* **setup:** project structure
* **support-page:** adds impersonation, closes [DIGIB2-630](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-630)
* **support-user-table:** adds explicit pageDto to openapi config, closes [DIGIB2-696](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-696)
* **support-user-table:** adds getProducer endpoint, closes [DIGIB2-696](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-696)
* **test-data:** add data-requests, closes [DIGIB2-729](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-729)
* **test-data:** add test data
* **testdata:** add endpoint for resetting test data
* **testdata:** add endpoint for resetting testdata on local, develop, integration
* **testing:** use keycloak devservices, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)
* **test:** move sonar configuration to sonar-project.properties, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)
* **translation-dto:** refactors translated fields into generic dto, closes [DIGIB2-467](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-467)
* **uid-register:** adds uid register api, closes [DIGIB2-544](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-544)
* **uid:** add manual deployment pipeline, closes [DIGIB2-715](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-715)
* **uid:** add uid to user-info, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)
* **uid:** determine authorized uids for data producer, closes [DIGIB2-485](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-485)
* **uid:** make uid of uid register a string, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)
* **uid:** reactivate uid interface
* **user-info:** add user-info endpoint, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)
* **wiremock:** adds wiremock test, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

### Bug Fixes

* **auth:** disable authentication for api /q/*
* **comments:** improvements for maintainability
* **consumer:** add delta endpoint for consumers, closes [DIGIB2-855](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-855)
* **create-consent-request:** adds api / crud to create a consent request, closes [DIGIB2-338](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-338)
* **datarequest-targetgroup:** adds target group to get dto, closes [DIGIB2-310](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-310)
* **deps:** update dependencies
* **javadoc:** adds javadoc as requested by BLW management
* **json-field-rewrite:** rename data masking to json field rename to cover inbound and outbound
* **logo:** add image/jpg as supported file type for the logo
* **redirect:** add redirect_uri regex to data_request
* **refactor:** rename table participant to users
* **swagger-pkce:** tries to fix swagger oauth with agate
* **swagger-pkce:** tries to fix swagger oauth with agate
* **swagger:** attempts to fix swagger / version on develop
* **swagger:** fixes version endpoint
* **test-data:** add db migration script locations to prevent error
* **uid:** disable uid register service because it is not available
* **uid:** fix uid / bur logic, closes [DIGIB2-715](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-715)
* **user-info:** fix user-info endpoint if missing attributes, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)
* **validation:** adjust input validation rules, closes [DIGIB2-343](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-343)

## 1.0.0-rc.99 (2025-10-13)

### Features

* **error-handling:** requestId not null, closes [DIGIB2-835](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-835)
* **pipeline:** fix pipeline

## 1.0.0-rc.98 (2025-10-13)

### Features

* **error-handling:** requestId not null, closes [DIGIB2-835](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-835)

## 1.0.0-rc.97 (2025-10-13)

### Features

* **pipeline:** fix pipeline

## 1.0.0-rc.96 (2025-10-13)

### Features

* **pipeline:** update main pipeline, closes [DIGIB2-898](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-898)

## 1.0.0-rc.95 (2025-10-09)

### Features

* **uid:** reactivate uid interface

## 1.0.0-rc.94 (2025-10-09)

### Features

* **open-source:** update THIRD_PARTY_LICENSES.md to use github dependency graph, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)

## 1.0.0-rc.93 (2025-10-06)

### Features

* **license:** update THIRD_PARTY_LICENSES.md

## 1.0.0-rc.92 (2025-10-02)

### Features

* **support-page:** adds impersonation, closes [DIGIB2-630](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-630)

## 1.0.0-rc.91 (2025-10-02)

### Features

* **open-source:** move license-check to weekly job, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)

## 1.0.0-rc.90 (2025-10-01)

### Features

* **open-source:** prepare for open-source, closes [DIGIB2-866](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-866)

## 1.0.0-rc.89 (2025-09-30)

### Features

* **migration:** add migratedFromMafDate, closes [DIGIB2-764](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-764)

## 1.0.0-rc.88 (2025-09-30)

### Features

* **support-user-table:** adds explicit pageDto to openapi config, closes [DIGIB2-696](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-696)

## 1.0.0-rc.87 (2025-09-26)

### Bug Fixes

* **redirect:** add redirect_uri regex to data_request

## 1.0.0-rc.86 (2025-09-22)

### Bug Fixes

* **test-data:** add db migration script locations to prevent error

## 1.0.0-rc.85 (2025-09-16)

### Features

* **support-user-table:** adds getProducer endpoint, closes [DIGIB2-696](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-696)

## 1.0.0-rc.84 (2025-09-12)

### Bug Fixes

* **consumer:** add delta endpoint for consumers, closes [DIGIB2-855](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-855)

## 1.0.0-rc.83 (2025-09-08)

### Bug Fixes

* **comments:** improvements for maintainability

## 1.0.0-rc.82 (2025-09-01)

### Features

* **test-data:** add data-requests, closes [DIGIB2-729](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-729)

## 1.0.0-rc.81 (2025-09-01)

### Bug Fixes

* **deps:** update dependencies

## 1.0.0-rc.80 (2025-08-29)

### Features

* **uid:** make uid of uid register a string, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)

## 1.0.0-rc.79 (2025-08-29)

### Features

* **uid:** add uid to user-info, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)

## 1.0.0-rc.78 (2025-08-29)

### Features

* **products:** add data products for mvp, closes [DIGIB2-694](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-694)

## 1.0.0-rc.77 (2025-08-28)

### Bug Fixes

* **auth:** disable authentication for api /q/*

## 1.0.0-rc.76 (2025-08-27)

### Bug Fixes

* **user-info:** fix user-info endpoint if missing attributes, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)

## 1.0.0-rc.75 (2025-08-27)

### Features

* **user-info:** add user-info endpoint, closes [DIGIB2-756](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-756)

## 1.0.0-rc.74 (2025-08-26)

### Features

* **api:** fix media type, closes [DIGIB2-338](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-338)

## 1.0.0-rc.73 (2025-08-25)

### Features

* **renovate:** update dependencies

## 1.0.0-rc.72 (2025-08-25)

### Features

* **renovate:** add dependency dashboard

## 1.0.0-rc.71 (2025-08-25)

### Features

* **renovate:** exclude patch versions from renovate bot

## 1.0.0-rc.70 (2025-08-25)

### Features

* **docu:** add code documentation, closes [DIGIB2-710](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-710)

## 1.0.0-rc.69 (2025-08-21)

### Bug Fixes

* **create-consent-request:** adds api / crud to create a consent request, closes [DIGIB2-338](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-338)

## 1.0.0-rc.68 (2025-08-18)

### Features

* **DIGIB2-639:** enhanced test data, so all cases can be tested & changed tests respectively, closes [DIGIB2-639](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-639)

## 1.0.0-rc.67 (2025-08-18)

### Features

* **cicd:** change configuration of renovate bot, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)

## 1.0.0-rc.66 (2025-08-15)

### Features

* **cicd:** add renovate bot, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)

## 1.0.0-rc.65 (2025-08-15)

### Features

* **deps:** update dependencies

## 1.0.0-rc.64 (2025-08-15)

### Features

* **uid:** add manual deployment pipeline, closes [DIGIB2-715](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-715)

## 1.0.0-rc.63 (2025-08-15)

### Features

* **cicd:** add manual deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)

## 1.0.0-rc.62 (2025-08-15)

### Features

* **cicd:** add manual deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)

## 1.0.0-rc.61 (2025-08-15)

### Features

* **cicd:** add manuel deployment pipeline, closes [DIGIB2-709](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-709)

## 1.0.0-rc.60 (2025-08-15)

### Bug Fixes

* **uid:** disable uid register service because it is not available
* **uid:** fix uid / bur logic, closes [DIGIB2-715](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-715)

## 1.0.0-rc.59 (2025-08-13)

### Features

* **data-transfer:** add tests, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)

## 1.0.0-rc.58 (2025-08-13)

### Bug Fixes

* **datarequest-targetgroup:** adds target group to get dto, closes [DIGIB2-310](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-310)

## 1.0.0-rc.57 (2025-08-12)

### Features

* **datarequest-products:** simplify products in dataRequest, closes [DIGIB2-310](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-310)

## 1.0.0-rc.56 (2025-08-12)

### Features

* **data-transfer:** adds consent request check, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)

## 1.0.0-rc.55 (2025-08-11)

### Features

* **refactoring:** split DataRequestService into three separate services

## 1.0.0-rc.54 (2025-08-08)

### Features

* **data-request:** add submissionDate and humanFriendlyId to data request, closes [DIGIB2-307](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-307)

## 1.0.0-rc.53 (2025-08-07)

### Features

* **data-request:** adds target group / changes submit to status endpoint, closes [DIGIB2-309](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-309)

## 1.0.0-rc.52 (2025-08-04)

### Features

* **consent:** add query param validation
* **data-transfer:** adds agis data transfer module, closes [DIGIB2-320](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-320)

## 1.0.0-rc.51 (2025-07-28)

### Features

* **agis:** update agis api from 2025-00 to 2025-01

## 1.0.0-rc.50 (2025-07-28)

### Features

* **log:** add debug logging to json field rewrite

## 1.0.0-rc.49 (2025-07-28)

### Bug Fixes

* **json-field-rewrite:** rename data masking to json field rename to cover inbound and outbound

## 1.0.0-rc.48 (2025-07-25)

### Features

* **test-data:** add test data

## 1.0.0-rc.47 (2025-07-25)

### Features

* **consent-status:** add endpoint for data request consent statuses

## 1.0.0-rc.46 (2025-07-21)

### Features

* **log:** add user id to log context

## 1.0.0-rc.45 (2025-07-21)

### Features

* **config:** remove profiles from application.yml to improve readability

## 1.0.0-rc.44 (2025-07-21)

### Features

* **api:** add data masking configuration for AGIS api, closes [DIGIB2-485](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-485)

## 1.0.0-rc.43 (2025-07-21)

### Features

* **uid:** determine authorized uids for data producer, closes [DIGIB2-485](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-485)

## 1.0.0-rc.42 (2025-07-18)

### Bug Fixes

* **logo:** add image/jpg as supported file type for the logo

## 1.0.0-rc.41 (2025-07-15)

### Bug Fixes

* **refactor:** rename table participant to users

## 1.0.0-rc.40 (2025-07-14)

### Bug Fixes

* **validation:** adjust input validation rules, closes [DIGIB2-343](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-343)

## 1.0.0-rc.39 (2025-07-09)

### Features

* **data-request:** add logo upload and contact information, closes [DIGIB2-403](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-403)

## 1.0.0-rc.38 (2025-07-04)

### Bug Fixes

* **swagger-pkce:** tries to fix swagger oauth with agate

## 1.0.0-rc.37 (2025-07-04)

### Features

* **data-request:** adds uid data to data request, closes [DIGIB2-487](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-487)

## 1.0.0-rc.36 (2025-07-02)

### Bug Fixes

* **swagger-pkce:** tries to fix swagger oauth with agate

## 1.0.0-rc.35 (2025-07-01)

### Features

* **uid-register:** adds uid register api, closes [DIGIB2-544](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-544)

## 1.0.0-rc.34 (2025-07-01)

### Features

* **data-products:** adds data products and data request CRUD, closes [DIGIB2-514](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-514)

## 1.0.0-rc.33 (2025-06-18)

### Bug Fixes

* **javadoc:** adds javadoc as requested by BLW management

## 1.0.0-rc.32 (2025-06-17)

### Features

* **translation-dto:** refactors translated fields into generic dto, closes [DIGIB2-467](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-467)

## 1.0.0-rc.31 (2025-06-16)

### Features

* **wiremock:** adds wiremock test, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

## 1.0.0-rc.30 (2025-06-13)

### Features

* **agis:** add role for endpoints, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

## 1.0.0-rc.29 (2025-06-12)

### Bug Fixes

* **swagger:** fixes version endpoint

## 1.0.0-rc.28 (2025-06-11)

### Bug Fixes

* **swagger:** attempts to fix swagger / version on develop

## 1.0.0-rc.27 (2025-06-11)

### Features

* **agis:** adds agis api, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

## 1.0.0-rc.26 (2025-06-11)

### Features

* **agis:** change path for certificates, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

## 1.0.0-rc.25 (2025-06-11)

### Features

* **agis:** add certificate, closes [DIGIB2-405](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-405)

## 1.0.0-rc.24 (2025-06-10)

### Features

* **authentication:** add quarkus dev console as valid redirect uri, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)

## 1.0.0-rc.23 (2025-06-06)

### Features

* **authentication:** add validation of audience claim, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)

## 1.0.0-rc.22 (2025-06-05)

### Features

* **cors:** configure cors, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)

## 1.0.0-rc.21 (2025-06-05)

### Features

* **authentication:** replace local dummy agate login ids with real ones for test data reliability, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)

## 1.0.0-rc.20 (2025-06-05)

### Features

* **authentication:** add token validation and protect endpoints, closes [DIGIB2-464](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-464)

## 1.0.0-rc.19 (2025-06-02)

### Features

* **testdata:** add endpoint for resetting testdata on local, develop, integration

## 1.0.0-rc.18 (2025-06-02)

### Features

* **data-request:** add title to data request, closes [DIGIB2-423](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-423)

## 1.0.0-rc.17 (2025-06-02)

### Features

* **pipeline:** trigger release and deployment, closes [DIGIB2-473](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-473)

## 1.0.0-rc.16 (2025-05-30)

### Features

* **audit:** add audit log functionality

## 1.0.0-rc.15 (2025-05-28)

### Features

* **testdata:** add endpoint for resetting test data

## 1.0.0-rc.14 (2025-05-28)

### Features

* **consent:** enable revert to opened state for consent requests, closes [DIGIB2-447](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-447)

## 1.0.0-rc.13 (2025-05-27)

### Features

* **authentication:** enable PKCE flow locally, closes [DIGIB2-462](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-462)

## 1.0.0-rc.12 (2025-05-26)

### Features

* **authentication:** match local access token with agate token, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)

## 1.0.0-rc.11 (2025-05-26)

### Features

* **authentication:** set local keycloak port, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)

## 1.0.0-rc.10 (2025-05-26)

### Features

* **api:** update consent request, closes [DIGIB2-435](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-435)

## 1.0.0-rc.9 (2025-05-26)

### Features

* **testing:** use keycloak devservices, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)

## 1.0.0-rc.8 (2025-05-23)

### Features

* **configuration:** make swagger available on dev, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)

## 1.0.0-rc.7 (2025-05-23)

### Features

* **initialize:** adds security configuration, closes [DIGIB2-371](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-371)

## 1.0.0-rc.6 (2025-05-22)

### Features

* **model:** replace table data_consumer with table participant, closes [DIGIB2-423](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-423)

## 1.0.0-rc.5 (2025-05-21)

### Features

* **initialize:** adds info endpoint, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)

## 1.0.0-rc.4 (2025-05-20)

### Features

* **test:** move sonar configuration to sonar-project.properties, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)

## 1.0.0-rc.3 (2025-05-20)

### Features

* **setup:** project structure

## 1.0.0-rc.2 (2025-05-15)

### Features

* **setup:** add checkstyle, owasp scan and trivy scan, closes [DIGIB2-411](https://blw-ofag-ufag.atlassian.net/browse/DIGIB2-411)

## 1.0.0-rc.1 (2025-05-13)

### Features

* **setup:** first version of data- and consent-requests
* **setup:** initial clean commit
