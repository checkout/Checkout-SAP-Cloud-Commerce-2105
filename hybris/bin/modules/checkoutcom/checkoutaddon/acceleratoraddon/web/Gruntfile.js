module.exports = function(grunt) {
  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    watch: {
      less: {
        files: ['webroot/WEB-INF/_ui-src/responsive/less/**'],
        tasks: ['less'],
      },
      synccoreviews: {
        files: ['webroot/WEB-INF/views/**'],
        tasks: ['sync:synccoreviews'],
      },
      synccoretags: {
        files: ['webroot/WEB-INF/tags/**'],
        tasks: ['sync:synccoretags'],
      },
      ybasejs: {
        files: ['webroot/_ui/responsive/common/js/**/*.js'],
        tasks: ['sync:syncybase'],
      },
    },
    less: {
      default: {
        files: [
          {
            expand: true,
            cwd: 'webroot/WEB-INF/_ui-src/responsive/less',
            src: '**/checkoutaddon.less',
            dest: 'webroot/_ui/responsive/common/css',
            ext: '.css'
          }
        ]
      },
    },
    sync : {
      synccoreviews: {
        files: [{
          cwd: 'webroot/WEB-INF/views/',
          src: '**',
          dest: '../../../../build/hybris/bin/modules/base-accelerator/yacceleratorstorefront/web/webroot/WEB-INF/views/addons/checkoutaddon',
        }]
      },
      synccoretags: {
        files: [{
          cwd: 'webroot/WEB-INF/tags/',
          src: '**',
          dest: '../../../../build/hybris/bin/modules/base-accelerator/yacceleratorstorefront/web/webroot/WEB-INF/tags/addons/checkoutaddon',
        }]
      },
      syncybase: {
        files: [{
          cwd: 'webroot/_ui/responsive/common/js',
          src: '**',
          dest: '../../../../build/hybris/bin/modules/base-accelerator/yacceleratorstorefront/web/webroot/_ui/addons/checkoutaddon/responsive/common/js',
        }]
      },
    }
    
});
 
  // Plugins
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-sync');


  // Default task(s). Run 'grunt watch' to start the watching task or add 'watch' to the task list and run 'grunt'.
  grunt.registerTask('default', ['sync', 'less', 'watch']);



};
