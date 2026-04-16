// Custom commands for StoreHub e2e tests

/**
 * Login via the UI form and verify redirect to dashboard.
 */
Cypress.Commands.add('login', (username, password) => {
  cy.visit('/auth/login');
  cy.get('#username').clear().type(username);
  cy.get('#password').clear().type(password);
  cy.get('button[type="submit"]').click();
  cy.url().should('include', '/dashboard');
});

/**
 * Login via API (faster, for setup steps).
 */
Cypress.Commands.add('apiLogin', (username, password) => {
  cy.request('POST', '/api/auth/login', { username, password }).then((resp) => {
    expect(resp.status).to.eq(200);
    window.localStorage.setItem('storehub_token', resp.body.token);
    window.localStorage.setItem(
      'storehub_user',
      JSON.stringify({
        username: resp.body.username,
        role: resp.body.role,
        siteId: resp.body.siteId,
      })
    );
  });
});

/**
 * Register a user via API.
 */
Cypress.Commands.add('apiRegister', (username, email, password) => {
  cy.request({
    method: 'POST',
    url: '/api/auth/register',
    body: { username, email, password },
    failOnStatusCode: false,
  }).then((resp) => {
    // May already exist from prior run — 200 or 400/409 are both OK here
    if (resp.status === 200) {
      return resp.body;
    }
  });
});
