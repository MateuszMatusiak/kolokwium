package edu.iis.mto.coffee;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.iis.mto.coffee.machine.*;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CoffeeMachineTest {
	private final int milkAmount = 80;

	@Mock
	private CoffeeMachine coffeeMachine;
	@Mock
	private CoffeeGrinder coffeeGrinder;
	@Mock
	private MilkProvider milkProvider;
	@Mock
	private CoffeeReceipes receipes;

	Map<CoffeeSize, Integer> waterAmounts;

	private CoffeeReceipe withMilk;
	private CoffeeReceipe withoutMilk;

	private CoffeeOrder doubleEspresso;
	private CoffeeOrder standardLatte;
	private CoffeeOrder smallCapuccino;
	private CoffeeOrder standardMachiatto;


	@BeforeEach
	void setUp() throws Exception {
		coffeeMachine = new CoffeeMachine(coffeeGrinder, milkProvider, receipes);

		waterAmounts = new HashMap<>();
		waterAmounts.put(CoffeeSize.SMALL, 80);
		waterAmounts.put(CoffeeSize.STANDARD, 150);
		waterAmounts.put(CoffeeSize.DOUBLE, 300);

		withoutMilk = CoffeeReceipe.builder().withMilkAmount(0).withWaterAmounts(waterAmounts).build();
		withMilk = CoffeeReceipe.builder().withMilkAmount(milkAmount).withWaterAmounts(waterAmounts).build();

		doubleEspresso = CoffeeOrder.builder().withSize(CoffeeSize.DOUBLE).withType(CoffeeType.ESPRESSO).build();
		standardLatte = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.LATTE).build();
		smallCapuccino = CoffeeOrder.builder().withSize(CoffeeSize.SMALL).withType(CoffeeType.CAPUCCINO).build();
		standardMachiatto = CoffeeOrder.builder().withSize(CoffeeSize.STANDARD).withType(CoffeeType.MACHIATTO).build();
	}


	@Test
	void properCoffeeShouldResultReady() throws GrinderException {
		when(receipes.getReceipe(any(CoffeeType.class))).thenReturn(withoutMilk);
		when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(false);

		Coffee res = coffeeMachine.make(doubleEspresso);
		assertEquals(res.getStatus(), Status.READY);
	}

	@Test
	void properLatteShouldInvokeElementsFromAddMilkMethod() throws GrinderException, HeaterException {
		when(receipes.getReceipe(any(CoffeeType.class))).thenReturn(withMilk);
		when(coffeeGrinder.grind(any(CoffeeSize.class))).thenReturn(false);
		when(milkProvider.pour(milkAmount)).thenReturn(milkAmount);

		coffeeMachine.make(standardLatte);
		InOrder callOrder = inOrder(milkProvider);

		callOrder.verify(milkProvider).heat();
		callOrder.verify(milkProvider).pour(milkAmount);
	}

	@Test
	void coffeeWithoutReceipesAndGrinderShouldResultError() {
		CoffeeOrder coffeeOrder = CoffeeOrder.builder().withSize(CoffeeSize.SMALL).withType(CoffeeType.ESPRESSO).build();

		Coffee res = coffeeMachine.make(doubleEspresso);
		assertEquals(res.getStatus(), Status.ERROR);
	}


}
