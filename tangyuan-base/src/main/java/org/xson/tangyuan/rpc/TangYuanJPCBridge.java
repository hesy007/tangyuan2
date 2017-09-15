package org.xson.tangyuan.rpc;

import java.net.URI;

import org.xson.common.object.XCO;
import org.xson.logging.Log;
import org.xson.logging.LogFactory;
import org.xson.tangyuan.TangYuanContainer;
import org.xson.tangyuan.TangYuanException;
import org.xson.tangyuan.executor.ServiceActuator;
import org.xson.tangyuan.executor.ServiceURI;
import org.xson.tangyuan.util.TangYuanUtil;
import org.xson.zongzi.JPCBridge;

public class TangYuanJPCBridge implements JPCBridge {

	private Log			log		= LogFactory.getLog(getClass());

	private JPCBridge	hook	= null;

	private Object doXcoRpcRquest(ServiceURI sURI, Object arg) throws Throwable {
		Object result = null;
		if (null == sURI.getMark()) {
			result = ServiceActuator.execute(sURI.getQualifiedServiceName(), arg);
		} else if ("async".equalsIgnoreCase(sURI.getMark())) {
			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
		} else if ("timer".equalsIgnoreCase(sURI.getMark())) {
			ServiceActuator.executeAsync(sURI.getQualifiedServiceName(), arg);
		} else {
			throw new TangYuanException("Invalid URL[mark]: " + sURI.getOriginal());
		}
		return result;
	}

	@Override
	public Object call(String path, Object arg) {
		log.info("client request to: " + path);
		log.info("client request args: " + arg);
		XCO result = null;
		try {
			ServiceURI sURI = ServiceURI.parseUrlPath(path);
			Object retObj = doXcoRpcRquest(sURI, arg);
			result = TangYuanUtil.retObjToXco(retObj);
		} catch (Throwable e) {
			result = TangYuanUtil.getExceptionResult(e);
		}
		return result;
	}

	@Override
	public URI inJvm(String url) {
		return this.hook.inJvm(url);
	}

	@Override
	public Object inJvmCall(URI uri, Object request) {
		return this.hook.inJvmCall(uri, request);
	}

	@Override
	public void setHook(Object obj) {
		this.hook = (JPCBridge) obj;
		RpcProxy.setJpc(this);
	}

	@Override
	public Object getServiceInfo() {
		return TangYuanContainer.getInstance().getServicesKeySet();
	}

}
