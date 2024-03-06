package com.cryptography.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.cryptography.services.RsaKeyService;

@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class SensitiveDataInterceptor {

	// @Around("@annotation(com.cryptography.annotations.SensitiveData)")
	@Around("execution(* com.cryptography.controllers..*Controller.*(..))")
	public Object doRequestResponseCryptography(final ProceedingJoinPoint pjp) throws Throwable {
		Object[] arguments = pjp.getArgs();
		this.encryptData(arguments);
		Object response = pjp.proceed(arguments);

		if (response instanceof ResponseEntity) {
			this.decryptData(Collections.singletonList(((ResponseEntity) response).getBody()).toArray());
		} else {
			this.decryptData(Collections.singletonList(response).toArray());
		}

		return response;
	}

	private void decryptData(final Object[] arguments) {
		this.decryptOrEncryptData(arguments, Boolean.TRUE);
	}

	private void encryptData(final Object[] arguments) {
		this.decryptOrEncryptData(arguments, Boolean.FALSE);
	}

	private void decryptOrEncryptData(final Object[] arguments, final Boolean decrypt) {
		Arrays.stream(arguments).filter(Objects::nonNull).forEach((object) -> {
			Arrays.stream(object.getClass().getDeclaredFields()).filter((field) -> {
				return field.isAnnotationPresent(SensitiveData.class);
			}).forEach((field) -> {
				try {
					this.alterModifierField(field);
					if (Objects.nonNull(field.get(object))) {
						Object fieldValue = field.get(object);
						if (fieldValue instanceof String) {
							this.tratarString(decrypt, object, field, (String) fieldValue);

						} else if (fieldValue instanceof List) {
							this.tratarLista(decrypt, object, field, (List) fieldValue);
						} else if (fieldValue instanceof Set) {
							this.tratarSet(decrypt, object, field, (Set) fieldValue);
						} else if (fieldValue instanceof Map) {
							// TODO tratar Map
							// this.tratarMap(decrypt, object, field, (Map) fieldValue);
						} else {
							this.decryptOrEncryptData(Collections.singletonList(fieldValue).toArray(), decrypt);
						}
					}

				} catch (Exception e) {
					System.err.println("Falha ao tratar dados sensíveis");
					e.printStackTrace();
				}
			});
		});
	}

	private void tratarString(final Boolean decrypt, final Object object, final Field field, final String fieldValue)
			throws Exception {
		System.out.println("Atributo +" + field + "+ passará pelo processo de descriptografia? " + decrypt);
		field.set(object, this.decryptOrEncryptString(decrypt, fieldValue));
	}

	@SuppressWarnings("unchecked")
	private void tratarSet(final Boolean decrypt, final Object object, final Field field, final Set<?> fieldValue)
			throws IllegalAccessException {

		System.out.println("Atributo +" + field + "+ passará pelo processo de descriptografia? " + decrypt);

		Optional<?> valueSet = fieldValue.stream().findFirst();
		if (valueSet.isPresent() && valueSet.get() instanceof String) {

			Set<String> collect = (Set) fieldValue.stream().map((item) -> {
				try {
					return this.decryptOrEncryptString(decrypt, (String) item);
				} catch (Exception e) {
				}
				return item;
			}).collect(Collectors.toSet());
			field.set(object, collect);
		} else {

			fieldValue.forEach((elem) -> {
				this.decryptOrEncryptData(Collections.singletonList(elem).toArray(), decrypt);
			});
		}
	}

	@SuppressWarnings("unchecked")
	private void tratarLista(final Boolean decrypt, final Object object, final Field field, final List<?> fieldValue)
			throws IllegalAccessException {

		System.out.println("Atributo +" + field + "+ passará pelo processo de descriptografia? " + decrypt);

		Optional<?> valueList = fieldValue.stream().findFirst();
		if (valueList.isPresent() && valueList.get() instanceof String) {
			List<String> collect = (List) fieldValue.stream().map((item) -> {
				try {
					return this.decryptOrEncryptString(decrypt, (String) item);
				} catch (Exception e) {
				}
				return item;
			}).collect(Collectors.toList());
			field.set(object, collect);
		} else {

			fieldValue.forEach((elem) -> {
				this.decryptOrEncryptData(Collections.singletonList(elem).toArray(), decrypt);
			});
		}
	}

	private String decryptOrEncryptString(final Boolean decrypt, final String item) throws Exception {
		try {
			if (Boolean.TRUE.equals(decrypt)) {
				return RsaKeyService.decrypt(item, RsaKeyService.myPrivatekey);
			} else {
				return RsaKeyService.encrypt(item, RsaKeyService.myPublickey);
			}
		} catch (GeneralSecurityException gsexc) {
			gsexc.printStackTrace();
			throw new Exception("Falha ao tratar dados sensíveis");
		}
	}

	private void alterModifierField(final Field field) throws NoSuchFieldException, IllegalAccessException {
		// Change private modifier to public
		field.setAccessible(true);

		// Remove final modifier
		// Field modifiersField = Field.class.getDeclaredField("modifiers");
		// modifiersField.setAccessible(true);
		// modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	}

}