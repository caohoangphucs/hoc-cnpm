package vn.id.caohoangphuc.cnpm.persistence.read;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Deliberately excludes save/delete; reads must use bounded pagination. */
@NoRepositoryBean
@Transactional(readOnly = true, transactionManager = "jpaReadTransactionManager")
public interface ReadRepository<T, ID> extends Repository<T, ID> {
    Optional<T> findById(ID id);
    Page<T> findAll(Pageable pageable);
    long count();
}
