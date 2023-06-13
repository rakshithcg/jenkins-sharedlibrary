def call(String registryname = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a', String depname = 'a', String contname = 'a') {

pipeline {
environment { 
    		registry = "${registryname}" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
    		deployment = "${depname}"
    		containerName = "${contname}"
	}
		
    agent none

    stages {
        stage("POLL SCM"){
		agent{label 'docker-1'}
            		steps {
                	checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])             
            		}
        } 
        
        stage('BUILD IMAGE') {
		agent{label 'docker-1'}
            		steps {
                	sh 'docker build -t $registry:$dockerTag .'             
            		}
        }
        
        stage('PUSH HUB') { 
		agent{label 'docker-1'}
            		steps {
			            sh 'docker push $registry:$dockerTag'                   	
                	}    
        }
        
        stage('DEPLOY IMAGE') {
		agent{label 'kubernetes'}
		          steps {
			            sh 'kubectl set image deploy $deployment $containerName="$registry:$dockerTag" --record'
		          }
	}  
    }
}  
}
