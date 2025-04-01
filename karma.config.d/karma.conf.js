config.set({
    browsers: ['ChromeHeadlessNoSandbox'],

    customLaunchers: {
        ChromeHeadlessNoSandbox: {
            base: 'ChromeHeadless',
            // Needed to work on Jenkins. Otherwise, there's an error:
            // 'Running as root without --no-sandbox is not supported. See https://crbug.com/638180.'
            flags: ['--no-sandbox'],
        }
    }
});