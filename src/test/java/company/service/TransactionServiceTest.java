package company.service;

import com.google.common.collect.Maps;
import company.domain.Transaction;
import company.dto.SumDTO;
import company.dto.TransactionDTO;
import company.exceptions.NotFoundException;
import company.model.TransactionDataSource;
import company.repository.TransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private TransactionDataSource dataSource;

    @Spy
    @InjectMocks
    private TransactionRepository transactionRepository = new TransactionRepository();

    @InjectMocks
    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception {
        setDataSourceField("transactions", Maps.newHashMap());
        setDataSourceField("types", Maps.newHashMap());
        setDataSourceField("sums", Maps.newHashMap());
    }

    @Test
    public void testGetTransactionById_found() {
        //given
        given(dataSource.getById(any())).willReturn(Transaction.builder().build());

        //when
        TransactionDTO dto = transactionService.getById(any());

        //then
        assertNotNull(dto);
    }

    @Test(expected = NotFoundException.class)
    public void testGetTransactionById_notFound() {
        //given
        given(dataSource.getById(any())).willThrow(new NotFoundException());

        //when
        transactionService.getById(any());
    }

    @Test
    public void testSaveTransaction_noParent() throws Exception {
        //given
        TransactionDTO dto = TransactionDTO.builder().amount(5000.0).type("cars").build();

        //when
        TransactionDTO savedDto = transactionService.saveTransaction(10L, dto);

        //then
        assertThat(savedDto, is(dto));
    }

    @Test(expected = NotFoundException.class)
    public void testSaveTransaction_noParentFound() throws Exception {
        //given
        TransactionDTO dto = TransactionDTO.builder().amount(5000.0).type("cars").parentId(101L).build();

        //when
        transactionService.saveTransaction(10L, dto);
    }

    @Test
    public void testCreateNewTransaction_transitiveSum() {
        //given
        TransactionDTO dto = TransactionDTO.builder().amount(25000.0).type("shopping").parentId(13L).build();

        //when
        transactionService.saveTransaction(10L, TransactionDTO.builder().amount(5000.0).type("cars").build());//parent first
        transactionService.saveTransaction(11L, TransactionDTO.builder().amount(10000.0).type("shopping").parentId(10L).build());//first child
        transactionService.saveTransaction(12L, TransactionDTO.builder().amount(15000.0).type("shopping").parentId(11L).build());//second child
        transactionService.saveTransaction(13L, TransactionDTO.builder().amount(20000.0).type("shopping").parentId(12L).build());//third child
        TransactionDTO savedDto = transactionService.saveTransaction(14L, dto);

        //then
        assertThat(savedDto, is(dto));
        assertThat(transactionRepository.getSumByTransactionId(10L).get(), is(75000.0));
        assertThat(transactionRepository.getSumByTransactionId(11L).get(), is(70000.0));
        assertThat(transactionRepository.getSumByTransactionId(12L).get(), is(60000.0));
        assertThat(transactionRepository.getSumByTransactionId(13L).get(), is(45000.0));
        assertThat(transactionRepository.getSumByTransactionId(14L).get(), is(25000.0));
    }

    @Test
    public void testGetTransactionSum_found() {
        //given
        given(dataSource.getSumByTransactionId(any())).willReturn(1000.0);

        //when
        SumDTO dto = transactionService.getSumByTransactionId(any());

        //then
        assertThat(dto.getSum(), is(1000.0));
    }

    @Test(expected = NotFoundException.class)
    public void testGetTransactionSum_notFound() {
        //given
        given(dataSource.getById(any())).willThrow(new NotFoundException());

        //when
        transactionService.getSumByTransactionId(any());
    }

    @Test
    public void testGetIdsByType_found() {
        //given
        transactionService.saveTransaction(10L, TransactionDTO.builder().amount(5000.0).type("cars").build());
        transactionService.saveTransaction(11L, TransactionDTO.builder().amount(10000.0).type("shopping").parentId(10L).build());
        transactionService.saveTransaction(12L, TransactionDTO.builder().amount(15000.0).type("shopping").parentId(11L).build());

        //when
        List<Long> ids = transactionService.getTransactionIdsByType("shopping");

        //then
        assertThat(ids, containsInAnyOrder(11L, 12L));

    }

    @Test
    public void testGetIdsByType_notFound() {
        //when
        List<Long> ids = transactionService.getTransactionIdsByType("shopping");

        //then
        assertTrue(ids.isEmpty());
    }

    private void setDataSourceField(String fieldName, Object fieldValue) throws Exception {
        Field field = TransactionDataSource.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(dataSource, fieldValue);
    }
}
