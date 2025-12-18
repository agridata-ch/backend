module.exports = {
    branchPrefix: 'renovate/',
    repositories: ['agridata-ch/backend'],
    extends: [':semanticCommitTypeAll(feat)', ":dependencyDashboard", ":dependencyDashboardApproval"],
    separateMinorPatch: true,
    patch: {
        enabled: false
    },
    packageRules: [
        {
            matchDatasources: ['maven', 'dockerfile', 'github-actions', 'maven-wrapper'],
            minimumReleaseAge: '90 days',
        },
    ],
};
