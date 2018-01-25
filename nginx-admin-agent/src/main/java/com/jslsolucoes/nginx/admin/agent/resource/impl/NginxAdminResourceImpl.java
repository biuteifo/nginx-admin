package com.jslsolucoes.nginx.admin.agent.resource.impl;

import java.io.File;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import com.jslsolucoes.file.system.FileSystemBuilder;
import com.jslsolucoes.nginx.admin.agent.resource.impl.nginx.NginxInfo;
import com.jslsolucoes.nginx.admin.agent.resource.impl.nginx.NginxInfoDiscover;
import com.jslsolucoes.nginx.admin.agent.resource.impl.os.OperationalSystem;
import com.jslsolucoes.nginx.admin.agent.resource.impl.os.OperationalSystemInfo;
import com.jslsolucoes.nginx.admin.agent.resource.impl.status.NginxStatus;
import com.jslsolucoes.nginx.admin.agent.resource.impl.status.NginxStatusDiscover;
import com.jslsolucoes.template.TemplateProcessor;

@RequestScoped
public class NginxAdminResourceImpl {
	
	private NginxInfoDiscover nginxInfoDiscover;
	private NginxStatusDiscover nginxStatusDiscover;
	
	@Deprecated
	public NginxAdminResourceImpl() {
		
	}
	
	@Inject
	public NginxAdminResourceImpl(NginxInfoDiscover nginxInfoDiscover,NginxStatusDiscover nginxStatusDiscover) {
		this.nginxInfoDiscover = nginxInfoDiscover;
		this.nginxStatusDiscover = nginxStatusDiscover;
	}

	public NginxOperationResult configure(String nginxHome, Integer maxPostSize,Boolean gzip) {
		try {
			createFileSystem(nginxHome);
			createTemplate(nginxHome,maxPostSize,gzip);
			return new NginxOperationResult(NginxOperationResultType.SUCCESS);
		} catch (Exception exception) {
			return new NginxOperationResult(NginxOperationResultType.ERROR,exception);
		}
	}

	private void createTemplate(String nginxHome, Integer maxPostSize,Boolean gzip) {
		TemplateProcessor
		.newBuilder()
			.withTemplate("/template/nginx/dynamic", "root.tpl")
			.withData("nginxHome", nginxHome)
			.withOutputLocation(new File(virtualHost(nginxHome), "root.conf"))
		.process()
		.clear()
			.withTemplate("/template/nginx/dynamic", "nginx.tpl")
			.withData("nginxHome", nginxHome)
			.withData("gzip", gzip)
			.withData("maxPostSize", maxPostSize)
			.withOutputLocation(new File(nginxHome, "nginx.conf"))
		.process();
	}
	
	private File virtualHost(String nginxHome) {
		return new File(nginxHome,"virtual-host");
	}	
	
	private void createFileSystem(String nginxHome) {
		FileSystemBuilder.newBuilder()
		.create()
			.withDestination(nginxHome)
			.execute()
		.end()
		.copy()
			.withClasspathResource("/template/nginx/fixed")
			.withDestination(nginxHome)
			.execute()
		.end();
	}

	public OperationalSystemInfo operationSystemInfo() {
		return OperationalSystem.info();
	}

	public NginxInfo nginxInfo(String nginxBin, String nginxHome) {
		return nginxInfoDiscover.info(nginxBin, nginxHome);
	}

	public NginxStatus status() {
		return nginxStatusDiscover.status();
	}
		
}
