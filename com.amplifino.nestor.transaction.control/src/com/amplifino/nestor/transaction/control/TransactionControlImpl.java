package com.amplifino.nestor.transaction.control;

import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionBuilder;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionRolledBackException;

@Component
public class TransactionControlImpl implements TransactionControl {
	
	private final ThreadLocal<TransactionScope> scopeHolder = ThreadLocal.withInitial(this::initialScope);
	@Reference
	private TransactionManager transactionManager;
	
	private TransactionScope getScope() {
		return scopeHolder.get();
	}
	
	private TransactionScope pushScope(TransactionScope scope) {
		scopeHolder.set(scope);
		return scope;
	}

	private void popScope() {
		scopeHolder.set(getScope().parent());
		getScope().resume();
	}
	
	@Override
	public <T> T notSupported(Callable<T> callable) throws TransactionException, ScopedWorkException {
		return execute(getScope().notSupported(), callable);			
	}

	@Override
	public <T> T required(Callable<T> callable) throws TransactionException, TransactionRolledBackException, ScopedWorkException {
		return execute(getScope().required(), callable);
	}

	@Override
	public <T> T requiresNew(Callable<T> callable) throws TransactionException, TransactionRolledBackException, ScopedWorkException {
		return execute(getScope().requiresNew(), callable);
	}

	@Override
	public <T> T supports(Callable<T> callable) throws TransactionException, ScopedWorkException {
		return execute(getScope().supports(), callable);
	}

	private  <T> T execute(TransactionScope scope, Callable<T> callable) {
		try {
			return pushScope(scope).execute(callable);
		} catch (Exception e) {
			throw new ScopedWorkException(e.toString(), e, getScope().getContext());
		} finally {
			popScope();
		}
	}
	
	@Override
	public boolean activeScope() {
		return getScope().isActive();
	}

	@Override
	public boolean activeTransaction() {
		return getScope().isTransaction();
	}

	@Override
	public TransactionBuilder build() {
		return new TransactionBuilderImpl(this);
	}

	@Override
	public TransactionContext getCurrentContext() {
		return getScope().getContext();
	}

	@Override
	public boolean getRollbackOnly() throws IllegalStateException {
		return getScope().getRollbackOnly();
	}

	@Override
	public void ignoreException(Throwable throwable) throws IllegalStateException {
		getScope().ignoreException(throwable);
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException {
		getScope().setRollbackOnly();
	}

	private TransactionScope initialScope() {
		return new NoScope(this);
	}
	
	TransactionManager transactionManager() {
		return transactionManager;
	}
}
