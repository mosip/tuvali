# Contributing

We value code contributions, but non-code contributions too, from writers, editors, testers, etc. are always welcomed. Join us to contribute
* To contribute to code, [repository](https://github.com/mosip/tuvali)
* To contribute to documentation, [repository](https://github.com/mosip/documentation/blob/inji/docs/integration-guide/tuvali-inji.md)

We want this community to be friendly and respectful to each other. Please follow it in all your interactions with the project.

## Code of Conduct
This project and everyone participating in it is governed by the [code of conduct](https://docs.mosip.io/1.2.0/community/code-of-conduct).

This guide will help you to contribute in various aspects like raising issues, feature request, etc.

## Project Initial Setup

To get started with the project, run `yarn` in the root directory to install the required dependencies for each package:

```sh
yarn
```

> While it's possible to use [`npm`](https://github.com/npm/cli), the tooling is built around [`yarn`](https://classic.yarnpkg.com/), so you'll have an easier time if you use `yarn` for development.

Make sure your code passes TypeScript and ESLint. Run the following to verify:

```sh
yarn typescript
yarn lint
```

To fix formatting errors, run the following:

```sh
yarn lint --fix
```

Remember to add tests for your change if possible. Run the unit tests by:

```sh
yarn test
```

To edit the Objective-C or Swift files, open `ios/` in XCode.

To edit the Java or Kotlin files, open `android/` in Android studio.

## Commit message convention

We follow the [conventional commits specification](https://www.conventionalcommits.org/en) for our commit messages:

- `fix`: bug fixes, e.g. fix crash due to deprecated method.
- `feat`: new features, e.g. add new method to the module.
- `refactor`: code refactor, e.g. migrate from class components to hooks.
- `docs`: changes into documentation, e.g. add usage example for the module..
- `test`: adding or updating tests, e.g. add integration tests using detox.
- `chore`: tooling changes, e.g. change CI config.

Our pre-commit hooks verify that your commit message matches this format when committing.

## Linting and tests

[ESLint](https://eslint.org/), [Prettier](https://prettier.io/), [TypeScript](https://www.typescriptlang.org/)

We use [TypeScript](https://www.typescriptlang.org/) for type checking, [ESLint](https://eslint.org/) with [Prettier](https://prettier.io/) for linting and formatting the code, and [Jest](https://jestjs.io/) for testing.

Our pre-commit hooks verify that the linter and tests pass when committing.

## Scripts

The `package.json` file contains various scripts for common tasks:

- `yarn bootstrap`: setup project by installing all dependencies and pods.
- `yarn typescript`: type-check files with TypeScript.
- `yarn lint`: lint files with ESLint.
- `yarn test`: run unit tests with Jest.

## Before Submitting a Pull Request

> **Working on your first pull request?** You can learn how from this _free_ series: [How to Contribute to an Open Source Project on GitHub](https://app.egghead.io/playlists/how-to-contribute-to-an-open-source-project-on-github).

**Before submitting your pull request** make sure the following requirements are fulfilled:

- Fork the repository
- Create a branch from `develop`
- Prefer small pull requests focused on one change
- Check linting and format it
- Change necessary code for bug fix, a new feature
- Follow the pull request template when opening a pull request
- For pull requests that change the API or implementation, discuss with maintainers first by opening an issue.


## Reporting an issue

Before submitting an issue, you need to make sure:

- Kindly provide an adequate description and a clear title
- If possible, share a URL towards the repository in which action is failing
