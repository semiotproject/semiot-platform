module.exports = function (grunt) {

    "use strict";

    // requirements
    var argv = require('yargs').argv;

    // custom options
    var BUILD_CONFIG = {
        src_dir: './src/',
        dist_dir: '../webapp/'
    };

    // legacy tasks
    require('load-grunt-tasks')(grunt);

    // grunt options
    grunt.initConfig({
        pkg: grunt.file.readJSON("package.json"),
        less: { // compile all LESS to dist dir
            index: {
                expand: true,
                cwd: BUILD_CONFIG.src_dir,
                src: ['css/**/*.less'],
                dest: BUILD_CONFIG.dist_dir,
                ext: '.css'
            }
        },
        browserify: {
            options: {
                browserifyOptions: {
                    // debug: true
                }
            },
            index: {
                src: BUILD_CONFIG.src_dir + "js/systems/index.js",
                dest: BUILD_CONFIG.dist_dir + "js/systems/index.js",
                options: {
                    browserifyOptions: {
                        debug: true
                    },
                    transform: ['babelify'],
                    watch: true
                }
            },
            wamp_client: {
                src: BUILD_CONFIG.src_dir + "js/wamp-client.js",
                dest: BUILD_CONFIG.dist_dir + "js/wamp-client.js",
                options: {
                    browserifyOptions: {
                        debug: true
                    },
                    transform: ['babelify'],
                    watch: true
                }
            }
        },
        eslint: {
            target: [BUILD_CONFIG.src_dir + '**/*.js']
        },
        uglify : {
            main : {
                options: {
                    sourceMap: true
                },
                src : BUILD_CONFIG.dist_dir + "js/systems/index.js",
                dest : BUILD_CONFIG.dist_dir + "js/systems/index.js",
            }
        },
        copy: {
            index: {
                files: [{
                    expand: true,
                    cwd: BUILD_CONFIG.src_dir,
                    src: ['**/*', '!**/*.less', '!**/*.js'],
                    dest: BUILD_CONFIG.dist_dir
                }]
            },
            fonts: {
                files: [{
                    expand: true,
                    cwd: './node_modules/font-awesome/fonts/',
                    src: ['*'],
                    dest: BUILD_CONFIG.dist_dir + "fonts/"
                }]
            },
            bower: {
                files: [{
                    expand: true,
                    flatten: true,
                    cwd: './bower_components/',
                    src: [
                        'angular-bootstrap-datetimepicker/src/js/datetimepicker.js',
                        'jquery/dist/jquery.js',
                        'angular/angular.js',
                        'moment/moment.js',
                        'highstock/highstock.js',
                        'highcharts-ng/dist/highcharts-ng.js',
                        'bootstrap/dist/js/bootstrap.min.js'
                    ],
                    dest: BUILD_CONFIG.dist_dir + "js/lib/"
                }, {
                    expand: true,
                    flatten: true,
                    cwd: './bower_components/',
                    src: [
                        'angular-bootstrap-datetimepicker/src/css/datetimepicker.css'
                    ],
                    dest: BUILD_CONFIG.dist_dir + "css/lib/"
                }, {
                    expand: true,
                    flatten: true,
                    cwd: './bower_components/',
                    src: [
                        'bootstrap/fonts/*'
                    ],
                    dest: BUILD_CONFIG.dist_dir + "fonts/"
                }]
            }
        },
        watch: { // watching all newer compilable files
            less: {
                files: BUILD_CONFIG.src_dir + "**/*.less",
                tasks: ['less'],
                options: {
                    livereload: true
                }
            },
            browserify: {
                files: BUILD_CONFIG.src_dir + "js/index.js",
                // no tasks, watchify gotcha, only for livereload
                options: {
                    debug: true,
                    livereload: true
                }
            },
            eslint: {
                files: BUILD_CONFIG.src_dir + "**/*.js",
                tasks: ['newer:eslint']
            },
            other: {
                files: [BUILD_CONFIG.src_dir + "**/*"].concat(["**/*.less", "**/*.js"].map(function(glob) {
                    return "!" + BUILD_CONFIG.src_dir + glob;
                })),
                tasks: ['copy'],
                options: {
                    livereload: true
                }
            }
        },
        'http-server': {
            'dev': {
                host: "0.0.0.0",
                root: BUILD_CONFIG.dist_dir,
                port: 3000,
                runInBackground: true,
                ext: "html"
            }
        }
    });

    var buildTasks = ["less", "browserify", "copy"];
    var prodTasks = ["uglify"];
    var watchTasks = ["http-server", "watch"];

    grunt.registerTask('build', buildTasks.concat(prodTasks));
    grunt.registerTask('serve', watchTasks);
    grunt.registerTask('dev', buildTasks.concat(watchTasks))

    grunt.registerTask("default", ["dev"]);
};

