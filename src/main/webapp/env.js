(function (window) {
	
	// Note: Type window.__env in a browser dev tools console to see the current values	
	window.__env = window.__env || {};
	
	// SITE MOCK .com
//	window.__env.siteMockHomeUrl = 'https://sitenv.org/home';
//	window.__env.siteMockEttUrl = 'https://ttpedge.sitenv.org/ttp/#/home';
//	window.__env.siteMockTestToolsUrl = 'https://sitenv.org/test-tools';
//	window.__env.siteMockSandboxCcdaUrl = 'https://sitenv.org/sandbox-ccda';
	
	// SITE MOCK .gov
	window.__env.siteMockHomeUrl = 'https://site.healthit.gov/home';
	window.__env.siteMockEttUrl = 'https://ett.healthit.gov/ett/#/home';
	window.__env.siteMockTestToolsUrl = 'https://site.healthit.gov/test-tools';
	window.__env.siteMockSandboxCcdaUrl = 'https://site.healthit.gov/sandbox-ccda';

	/*
	Overwrite this file with appropriate environment file in ../resources/env
	-or just overwrite this comment with the relevant data
	-or simply reference the data when updating a pre-deployed build, as, 
	this data does not require a build/rebuild to resolve
	 
	Current environments are:
	env-com-prod.js
			https://sitenv.org
	env-gov-prod.js
			https://site.healthit.gov
	*/
	
}(this));
