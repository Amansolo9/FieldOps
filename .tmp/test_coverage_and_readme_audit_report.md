# Test Coverage Audit

## Backend Endpoint Inventory
- `DELETE /api/addresses/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:46
- `DELETE /api/community/posts/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:70
- `DELETE /api/community/topics/{topic}/follow` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:117
- `DELETE /api/community/users/{userId}/follow` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:186
- `DELETE /api/delivery-zone-groups/{groupId}/bands/{bandId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:72
- `DELETE /api/delivery-zone-groups/{groupId}/zips/{zipCode}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:50
- `DELETE /api/delivery-zones/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:52
- `DELETE /api/users/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:53
- `GET /api/addresses` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:30
- `GET /api/admin/incentive-rules` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:20
- `GET /api/analytics/experiments` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:74
- `GET /api/analytics/experiments/{name}/bucket` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:80
- `GET /api/analytics/sites/{siteId}/metrics` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:45
- `GET /api/analytics/sites/{siteId}/retention` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:57
- `GET /api/audit/entity/{entityType}/{entityId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:33
- `GET /api/audit/range` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:57
- `GET /api/audit/user/{userId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:45
- `GET /api/checkins/fraud-alerts` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:48
- `GET /api/checkins/site/{siteId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:38
- `GET /api/community/favorites` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:168
- `GET /api/community/following` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:195
- `GET /api/community/points/{userId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:142
- `GET /api/community/points/me` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:135
- `GET /api/community/posts` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:46
- `GET /api/community/posts/{postId}/comments` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:89
- `GET /api/community/posts/following` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:63
- `GET /api/community/posts/topic/{topic}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:54
- `GET /api/community/quarantine/pending` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:204
- `GET /api/community/topics/following` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:126
- `GET /api/credit-score/{userId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CreditScoreController.java:30
- `GET /api/credit-score/me` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CreditScoreController.java:24
- `GET /api/delivery-zone-groups/site/{siteId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:23
- `GET /api/delivery-zones/site/{siteId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:23
- `GET /api/orders/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:39
- `GET /api/orders/{id}/shipping-label` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:79
- `GET /api/orders/my` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:47
- `GET /api/orders/site/{siteId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:55
- `GET /api/organizations` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:29
- `GET /api/organizations/{parentId}/children` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:41
- `GET /api/organizations/level/{level}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:35
- `GET /api/ratings/appeals/pending` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:67
- `GET /api/ratings/user/{userId}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:34
- `GET /api/ratings/user/{userId}/average` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:40
- `GET /api/tickets/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:39
- `GET /api/tickets/{id}/evidence` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:85
- `GET /api/tickets/evidence/{evidenceId}/verify` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:95
- `GET /api/tickets/my` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:47
- `GET /api/tickets/status/{status}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:54
- `GET /api/users` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:28
- `GET /api/users/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:40
- `GET /api/users/me` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:34
- `PATCH /api/admin/incentive-rules/{actionKey}/toggle` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:35
- `PATCH /api/analytics/experiments/{id}/deactivate` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:107
- `PATCH /api/checkins/fraud-alerts/{id}/resolve` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:54
- `PATCH /api/community/quarantine/{id}/review` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:210
- `PATCH /api/delivery-zone-groups/{groupId}/deactivate` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:82
- `PATCH /api/orders/{id}/status` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:63
- `PATCH /api/ratings/{id}/appeal/resolve` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:56
- `PATCH /api/tickets/{id}/assign` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:68
- `PATCH /api/tickets/{id}/status` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:60
- `PATCH /api/users/{id}/role` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:46
- `POST /api/addresses` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:22
- `POST /api/analytics/events` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:34
- `POST /api/analytics/experiments` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:67
- `POST /api/analytics/experiments/{id}/rollback` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:114
- `POST /api/analytics/experiments/{name}/outcome` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:88
- `POST /api/auth/login` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuthController.java:19
- `POST /api/auth/register` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuthController.java:24
- `POST /api/checkins` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:29
- `POST /api/community/posts` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:38
- `POST /api/community/posts/{postId}/comments` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:80
- `POST /api/community/posts/{postId}/favorite` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:159
- `POST /api/community/posts/{postId}/vote` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:97
- `POST /api/community/topics/{topic}/follow` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:108
- `POST /api/community/users/{userId}/follow` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:177
- `POST /api/delivery-zone-groups` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:30
- `POST /api/delivery-zone-groups/{groupId}/bands` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:60
- `POST /api/delivery-zone-groups/{groupId}/zips` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:39
- `POST /api/delivery-zones` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:30
- `POST /api/orders` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:31
- `POST /api/orders/{id}/verify-pickup` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:70
- `POST /api/organizations` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:22
- `POST /api/ratings` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:26
- `POST /api/ratings/{id}/appeal` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:47
- `POST /api/tickets` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:31
- `POST /api/tickets/{id}/evidence` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:76
- `POST /api/users/reauth` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:61
- `PUT /api/addresses/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:37
- `PUT /api/admin/incentive-rules/{actionKey}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:26
- `PUT /api/analytics/experiments/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:99
- `PUT /api/delivery-zones/{id}` - repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:40

Total resolved endpoints from controllers: **91**

## API Test Mapping Table
| Endpoint | Covered | Test Type | Test Files | Evidence (file + function/test reference) |
|---|---|---|---|---|
| `DELETE /api/addresses/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:46; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:253 (delete_api_addresses_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/community/posts/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:70; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:541 (delete_api_community_posts_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/community/topics/{topic}/follow` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:117; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:465 (delete_api_community_topics_follow); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/community/users/{userId}/follow` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:186; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:490 (delete_api_community_users_follow); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/delivery-zone-groups/{groupId}/bands/{bandId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:72; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:869 (delete_api_delivery_zone_groups_bands); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/delivery-zone-groups/{groupId}/zips/{zipCode}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:50; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:843 (delete_api_delivery_zone_groups_zips); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/delivery-zones/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:52; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:796 (delete_api_delivery_zones_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `DELETE /api/users/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:53; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1077 (delete_api_users_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/addresses` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:30; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:234 (get_api_addresses); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/admin/incentive-rules` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:20; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1043 (get_api_admin_incentive_rules); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/analytics/experiments` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:74; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:941 (get_api_analytics_experiments); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/analytics/experiments/{name}/bucket` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:80; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:950 (get_api_analytics_experiments_bucket); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/analytics/sites/{siteId}/metrics` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:45; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:910 (get_api_analytics_sites_metrics); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/analytics/sites/{siteId}/retention` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:57; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:920 (get_api_analytics_sites_retention); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/audit/entity/{entityType}/{entityId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:33; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1014 (get_api_audit_entity); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/audit/range` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:57; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1030 (get_api_audit_range); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/audit/user/{userId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuditController.java:45; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1022 (get_api_audit_user); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/checkins/fraud-alerts` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:48; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:363 (get_api_checkins_fraud_alerts); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/checkins/site/{siteId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:38; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:355 (get_api_checkins_site); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/favorites` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:168; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:515 (get_api_community_favorites); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/following` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:195; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:481 (get_api_community_following); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/points/{userId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:142; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:533 (get_api_community_points_userId); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/points/me` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:135; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:524 (get_api_community_points_me); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/posts` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:46; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:403 (get_api_community_posts); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/posts/{postId}/comments` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:89; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:439 (get_api_community_posts_comments); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/posts/following` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:63; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:498 (get_api_community_posts_following); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/posts/topic/{topic}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:54; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:412 (get_api_community_posts_topic); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/quarantine/pending` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:204; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:555 (get_api_community_quarantine_pending); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/community/topics/following` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:126; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:456 (get_api_community_topics_following); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/credit-score/{userId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CreditScoreController.java:30; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:751 (get_api_credit_score_userId); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/credit-score/me` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CreditScoreController.java:24; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:742 (get_api_credit_score_me); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/delivery-zone-groups/site/{siteId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:23; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:823 (get_api_delivery_zone_groups_site); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/delivery-zones/site/{siteId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:23; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:776 (get_api_delivery_zones_site); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/orders/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:39; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:282 (get_api_orders_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/orders/{id}/shipping-label` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:79; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:332 (get_api_orders_id_shipping_label); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/orders/my` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:47; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:291 (get_api_orders_my); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/orders/site/{siteId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:55; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:300 (get_api_orders_site); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/organizations` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:29; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:195 (get_api_organizations); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/organizations/{parentId}/children` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:41; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:211 (get_api_organizations_children); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/organizations/level/{level}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:35; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:203 (get_api_organizations_level); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/ratings/appeals/pending` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:67; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:615 (get_api_ratings_appeals_pending); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/ratings/user/{userId}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:34; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:588 (get_api_ratings_user); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/ratings/user/{userId}/average` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:40; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:597 (get_api_ratings_user_average); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/tickets/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:39; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:651 (get_api_tickets_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/tickets/{id}/evidence` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:85; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:717 (get_api_tickets_evidence); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/tickets/evidence/{evidenceId}/verify` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:95; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:726 (get_api_tickets_evidence_verify); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/tickets/my` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:47; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:660 (get_api_tickets_my); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/tickets/status/{status}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:54; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:669 (get_api_tickets_status); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/users` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:28; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:131 (get_api_users); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/users/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:40; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:154 (get_api_users_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `GET /api/users/me` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:34; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:145 (get_api_users_me); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/admin/incentive-rules/{actionKey}/toggle` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:35; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1062 (patch_api_admin_incentive_rules_toggle); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/analytics/experiments/{id}/deactivate` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:107; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:984 (patch_api_analytics_experiments_deactivate); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/checkins/fraud-alerts/{id}/resolve` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:54; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:371 (patch_api_checkins_fraud_alerts_resolve); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/community/quarantine/{id}/review` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:210; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:563 (patch_api_community_quarantine_review); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/delivery-zone-groups/{groupId}/deactivate` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:82; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:882 (patch_api_delivery_zone_groups_deactivate); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/orders/{id}/status` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:63; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:308 (patch_api_orders_id_status); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/ratings/{id}/appeal/resolve` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:56; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:624 (patch_api_ratings_appeal_resolve); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/tickets/{id}/assign` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:68; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:677 (patch_api_tickets_assign); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/tickets/{id}/status` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:60; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:689 (patch_api_tickets_status); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PATCH /api/users/{id}/role` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:46; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:163 (patch_api_users_id_role); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/addresses` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:22; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:223 (post_api_addresses); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/analytics/events` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:34; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:897 (post_api_analytics_events); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/analytics/experiments` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:67; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:928 (post_api_analytics_experiments); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/analytics/experiments/{id}/rollback` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:114; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:999 (post_api_analytics_experiments_rollback); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/analytics/experiments/{name}/outcome` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:88; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:960 (post_api_analytics_experiments_outcome); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/auth/login` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuthController.java:19; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:91 (post_api_auth_login); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/auth/register` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AuthController.java:24; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:104 (post_api_auth_register); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/checkins` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CheckInController.java:29; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:345 (post_api_checkins); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/posts` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:38; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:392 (post_api_community_posts); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/posts/{postId}/comments` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:80; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:429 (post_api_community_posts_comments); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/posts/{postId}/favorite` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:159; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:506 (post_api_community_posts_favorite); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/posts/{postId}/vote` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:97; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:420 (post_api_community_posts_vote); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/topics/{topic}/follow` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:108; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:448 (post_api_community_topics_follow); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/community/users/{userId}/follow` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/CommunityController.java:177; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:473 (post_api_community_users_follow); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/delivery-zone-groups` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:30; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:811 (post_api_delivery_zone_groups); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/delivery-zone-groups/{groupId}/bands` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:60; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:854 (post_api_delivery_zone_groups_bands); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/delivery-zone-groups/{groupId}/zips` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneGroupController.java:39; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:832 (post_api_delivery_zone_groups_zips); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/delivery-zones` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:30; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:764 (post_api_delivery_zones); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/orders` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:31; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:265 (post_api_orders); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/orders/{id}/verify-pickup` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrderController.java:70; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:322 (post_api_orders_id_verify_pickup); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/organizations` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/OrganizationController.java:22; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:181 (post_api_organizations); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/ratings` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:26; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:576 (post_api_ratings); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/ratings/{id}/appeal` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/RatingController.java:47; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:606 (post_api_ratings_appeal); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/tickets` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:31; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:640 (post_api_tickets); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/tickets/{id}/evidence` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/SupportTicketController.java:76; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:701 (post_api_tickets_evidence); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `POST /api/users/reauth` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/UserController.java:61; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:120 (post_api_users_reauth); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PUT /api/addresses/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AddressController.java:37; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:243 (put_api_addresses_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PUT /api/admin/incentive-rules/{actionKey}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/IncentiveRuleController.java:26; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:1051 (put_api_admin_incentive_rules); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PUT /api/analytics/experiments/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/AnalyticsController.java:99; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:968 (put_api_analytics_experiments_id); repo/e2e/cypress/e2e/api-coverage.cy.js |
| `PUT /api/delivery-zones/{id}` | yes | true no-mock HTTP | `EndpointCoverageIntegrationTest.java`, `api-coverage.cy.js` | repo/backend/src/main/java/com/eaglepoint/storehub/controller/DeliveryZoneController.java:40; repo/backend/tests/integration/EndpointCoverageIntegrationTest.java:785 (put_api_delivery_zones_id); repo/e2e/cypress/e2e/api-coverage.cy.js |

## API Test Classification
1. **True No-Mock HTTP**
- 
epo/backend/tests/integration/EndpointCoverageIntegrationTest.java (@SpringBootTest, TestRestTemplate, 91 endpoint-addressed tests)
- 
epo/backend/tests/integration/BusinessFlowIntegrationTest.java (@SpringBootTest, real HTTP business flows)
- 
epo/backend/tests/integration/FullStackIntegrationTest.java (@SpringBootTest, real HTTP + deep assertions)
- 
epo/e2e/cypress/e2e/api-coverage.cy.js (cy.request to real /api/... endpoints, no cy.intercept)

2. **HTTP with Mocking**
- 
epo/backend/tests/api_tests/MockMvcAuthorizationTest.java (@WebMvcTest + multiple @MockitoBean service/repository overrides)
- 
epo/backend/tests/api_tests/PayloadValidationTest.java (@WebMvcTest + multiple @MockitoBean overrides)

3. **Non-HTTP (unit/integration without HTTP)**
- 
epo/backend/tests/api_tests/EndpointAuthorizationTest.java
- 
epo/backend/tests/api_tests/HttpAuthorizationMatrixTest.java
- 
epo/backend/tests/api_tests/SecurityIntegrationTest.java
- 
epo/backend/tests/api_tests/SupervisorNoteValidationTest.java
- 
epo/backend/tests/unit_tests/*.java

## Mock Detection (Strict)
- Mocked controllers/services/repositories in HTTP tests:
  - 
epo/backend/tests/api_tests/MockMvcAuthorizationTest.java lines 134-161 (@MockitoBean for service/repository graph)
  - 
epo/backend/tests/api_tests/PayloadValidationTest.java lines 83-96 (@MockitoBean for service/repository graph)
- Direct service/mock usage (non-HTTP):
  - 
epo/backend/tests/api_tests/EndpointAuthorizationTest.java (mocked OrganizationRepository in SiteAuthorizationService)
  - 
epo/backend/tests/api_tests/HttpAuthorizationMatrixTest.java (mocked OrganizationRepository)
  - 
epo/backend/tests/api_tests/SecurityIntegrationTest.java (mocked auth/security context components)

## Coverage Summary
- Total endpoints: **91**
- Endpoints with HTTP tests: **91**
- Endpoints with true no-mock HTTP tests: **91**
- HTTP coverage: **100.0%**
- True API coverage: **100.0%**

## Unit Test Summary
- Unit/API non-HTTP test files present: **39+** under 
epo/backend/tests/unit_tests and 
epo/backend/tests/api_tests.
- Modules covered:
  - Services: auth, orders, check-ins, ratings, tickets, community, analytics, experiments, addresses, delivery-zone/group, credit-score, incentives, audit, SLA, retention, bootstrap, favorites, follows
  - Security/scope logic: site authorization, data-scope enforcement, idempotency filter, JWT provider
  - Cross-cutting: audit aspect, validation-specific service logic
- Important modules not directly tested (dedicated direct tests absent):
  - 
epo/backend/src/main/java/com/eaglepoint/storehub/security/JwtAuthenticationFilter.java
  - 
epo/backend/src/main/java/com/eaglepoint/storehub/config/RateLimitFilter.java
  - 
epo/backend/src/main/java/com/eaglepoint/storehub/config/SecurityConfig.java
  - Repository-layer behavior is mostly covered indirectly via integration tests, but dedicated repository tests are not present.

## API Observability Check
- Strong in true no-mock suite:
  - Endpoint visibility: explicit in test names (@DisplayName("METHOD /api/...") and Cypress it('METHOD /api/...')
  - Request input visibility: explicit JSON/query/params in calls
  - Response visibility: status + body assertions across integration and Cypress suites
- Weak spots:
  - Some Cypress coverage cases accept fallback status (ailOnStatusCode: false) and are less strict for negative paths (e.g., evidence upload, rollback).

## Tests Check
- 
un_tests.sh is Docker-based for backend/frontend/e2e:
  - docker-compose run --rm --no-deps backend mvn test -B (line 26)
  - docker-compose run --rm --no-deps frontend ... (lines 38, 50)
  - docker-compose -f docker-compose.e2e.yml up --build ... (line 84)
- No mandatory local package-manager install is required by the script itself.

## Test Quality & Sufficiency
- Success paths: strong and broad (full 91 endpoint sweep + business flow integration).
- Failure/edge coverage: present, but uneven by endpoint; not every endpoint has rich negative-case assertions in the true no-mock suite.
- Auth/permissions: strong (MockMvc matrix + integration flows + role transitions).
- Integration boundaries: strong (real DB via Testcontainers in backend integration; FE↔BE Cypress in docker-compose e2e).
- Assertion depth: generally meaningful; still mixed strictness in some Cypress steps.

## End-to-End Expectations (Fullstack)
- Fullstack FE↔BE tests exist (
epo/e2e/cypress/e2e/*.cy.js) and are wired in 
un_tests.sh using docker-compose.e2e.yml.
- This satisfies fullstack expectation.

## Test Coverage Score (0-100)
**92/100**

## Score Rationale
- + Full endpoint inventory covered with true no-mock HTTP tests (major uplift).
- + Strong integration breadth across API and FE↔BE e2e.
- + Substantial unit/service test suite.
- - Mock-heavy @WebMvcTest suites remain for many authorization/validation checks.
- - Some endpoint tests are status-centric or use permissive ailOnStatusCode: false, reducing strictness for negative assertions.
- - Missing direct tests for core security filters/config classes.

## Key Gaps
1. Add direct tests for JwtAuthenticationFilter, RateLimitFilter, and SecurityConfig behavior.
2. Tighten permissive Cypress cases that currently allow multiple statuses without stronger assertions.
3. Expand negative-path assertions for critical endpoints in true no-mock integration suite.

## Confidence & Assumptions
- Confidence: **High**.
- Assumptions:
  - Static-only audit; no runtime execution performed.
  - Endpoint coverage determination is based on controller route extraction and static route-to-test diff (91/91, missing=0, extra=0).

## Test Coverage Verdict
**PASS (with quality caveats)**

---

# README Audit

## README Location
- Found at required path: 
epo/README.md

## Project Type Detection
- Explicitly declared at top: **Fullstack** (
epo/README.md:3)

## Hard Gate Evaluation
### Formatting
- PASS: Markdown is structured and readable.

### Startup Instructions (Fullstack)
- PASS: Includes docker-compose up (
epo/README.md:14).

### Access Method
- PASS: Includes frontend and API URLs/ports (
epo/README.md:17-18).

### Verification Method
- PASS: Includes API curl verification and UI login flow (
epo/README.md:73-88).

### Environment Rules (Docker-contained, no manual DB/runtime installs)
- PASS (strict static): README states Docker-only prerequisite and no manual DB/.env required for evaluation (
epo/README.md:8, 
epo/README.md:22, 
epo/README.md:407).
- Note: Quick setup examples use jq in local shell snippets (
epo/README.md:38+), which is a soft portability concern.

### Demo Credentials (Auth present)
- PASS: Credentials provided for all declared roles, with username/password (
epo/README.md:26-33).

## Engineering Quality
- Tech stack clarity: strong.
- Architecture explanation: strong.
- Testing instructions: strong, includes dockerized backend/frontend/e2e commands.
- Security/roles/workflows: detailed.
- Presentation quality: high, though long and somewhat dense.

## High Priority Issues
- None.

## Medium Priority Issues
1. jq dependency appears in quick-setup/verification snippets; this may reduce out-of-box portability for users without jq.

## Low Priority Issues
1. README is very long; critical bootstrap path could be separated from advanced remediation/reference notes for faster onboarding.

## Hard Gate Failures
- None.

## README Verdict
**PASS**

---

# Final Verdicts
- **Test Coverage Audit:** PASS (score **92/100**)
- **README Audit:** PASS
