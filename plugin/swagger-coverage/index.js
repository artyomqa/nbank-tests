'use strict';

allure.api.addTranslation('en', {
    tab: {
        swaggerCoverage: {
            name: 'API coverage',
        },
    },
});

allure.api.addTranslation('ru', {
    tab: {
        swaggerCoverage: {
            name: 'API покрытие',
        },
    },
});

allure.api.addTab('swagger-coverage', {
    title: 'tab.swaggerCoverage.name',
    icon: 'fa fa-shield',
    route: 'swagger-coverage',
    onEnter: function () {
        return new Backbone.Marionette.View({
            className: 'pane__section',
            template: function () {
                return (
                    '<h3 class="pane__section-title">API coverage</h3>' +
                    '<iframe src="swagger-coverage/latest/swagger-coverage-report.html" ' +
                    'style="width:100%; height:80vh; border:none;"></iframe>'
                );
            },
        });
    },
});
