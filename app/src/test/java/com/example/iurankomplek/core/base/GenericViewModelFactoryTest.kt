package com.example.iurankomplek.core.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SampleViewModel : ViewModel() {
    var value: String = "initial"
}

class GenericViewModelFactoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var factory: GenericViewModelFactory<SampleViewModel>
    private lateinit var testViewModel: SampleViewModel

    @Before
    fun setup() {
        testViewModel = SampleViewModel()
        factory = GenericViewModelFactory(SampleViewModel::class.java) { testViewModel }
    }

    @Test
    fun `create returns correct ViewModel instance`() {
        val createdViewModel = factory.create(SampleViewModel::class.java)

        assertNotNull("Created ViewModel should not be null", createdViewModel)
        assertSame("Created ViewModel should be the same instance", testViewModel, createdViewModel)
    }

    @Test
    fun `create returns ViewModel of correct type`() {
        val createdViewModel = factory.create(SampleViewModel::class.java)

        assertTrue("Created ViewModel should be of correct type", createdViewModel is SampleViewModel)
    }

    @Test
    fun `create uses provided creator function`() {
        val expectedViewModel = SampleViewModel()
        expectedViewModel.value = "created"

        val customFactory = GenericViewModelFactory(SampleViewModel::class.java) { expectedViewModel }
        val createdViewModel = customFactory.create(SampleViewModel::class.java)

        assertEquals("Creator function should be used", "created", (createdViewModel as SampleViewModel).value)
    }

    @Test
    fun `create with different ViewModel class throws IllegalArgumentException`() {
        class DifferentViewModel : ViewModel()

        val exception = try {
            factory.create(DifferentViewModel::class.java)
            null
        } catch (e: IllegalArgumentException) {
            e
        }

        assertNotNull("Exception should be thrown for wrong ViewModel class", exception)
        assertTrue("Exception message should contain class name", exception?.message?.contains("DifferentViewModel") ?: false)
    }

    @Test
    fun `create with subclass ViewModel class returns correct instance`() {
        class SubclassViewModel : SampleViewModel() {
            var subValue: String = "subclass"
        }

        val subclassInstance = SubclassViewModel()
        val subclassFactory = GenericViewModelFactory(SubclassViewModel::class.java) { subclassInstance }
        val createdViewModel = subclassFactory.create(SubclassViewModel::class.java)

        assertSame("Subclass ViewModel should be created", subclassInstance, createdViewModel)
    }

    @Test
    fun `create with same class multiple times returns same instance`() {
        val viewModel1 = factory.create(SampleViewModel::class.java)
        val viewModel2 = factory.create(SampleViewModel::class.java)

        assertSame("Multiple create calls should return same instance", viewModel1, viewModel2)
    }

    @Test
    fun `create returns ViewModel with initialized properties`() {
        testViewModel.value = "test_value"

        val createdViewModel = factory.create(SampleViewModel::class.java)

        assertEquals("ViewModel should have initialized properties", "test_value", (createdViewModel as SampleViewModel).value)
    }

    @Test
    fun `create handles ViewModel with constructor parameters`() {
        class ViewModelWithParam(val param: String) : ViewModel()

        val expectedViewModel = ViewModelWithParam("test_param")
        val paramFactory = GenericViewModelFactory(ViewModelWithParam::class.java) { expectedViewModel }
        val createdViewModel = paramFactory.create(ViewModelWithParam::class.java)

        assertEquals("ViewModel parameter should be preserved", "test_param", (createdViewModel as ViewModelWithParam).param)
    }

    @Test
    fun `create handles ViewModel with complex constructor`() {
        class ComplexViewModel(val param1: String, val param2: Int, val param3: Boolean) : ViewModel()

        val expectedViewModel = ComplexViewModel("test", 42, true)
        val complexFactory = GenericViewModelFactory(ComplexViewModel::class.java) { expectedViewModel }
        val createdViewModel = complexFactory.create(ComplexViewModel::class.java)

        val vm = createdViewModel as ComplexViewModel
        assertEquals("Param1 should match", "test", vm.param1)
        assertEquals("Param2 should match", 42, vm.param2)
        assertEquals("Param3 should match", true, vm.param3)
    }

    @Test
    fun `create handles ViewModel with nullable parameters`() {
        class NullableViewModel(val param: String?) : ViewModel()

        val expectedViewModel = NullableViewModel(null)
        val nullableFactory = GenericViewModelFactory(NullableViewModel::class.java) { expectedViewModel }
        val createdViewModel = nullableFactory.create(NullableViewModel::class.java)

        assertNull("Nullable parameter should be null", (createdViewModel as NullableViewModel).param)
    }

    @Test
    fun `create with ViewModel from different package works correctly`() {
        class PackageViewModel(val data: Int) : ViewModel()

        val expectedViewModel = PackageViewModel(123)
        val packageFactory = GenericViewModelFactory(PackageViewModel::class.java) { expectedViewModel }
        val createdViewModel = packageFactory.create(PackageViewModel::class.java)

        assertEquals("ViewModel from different package should work", 123, (createdViewModel as PackageViewModel).data)
    }

    @Test
    fun `create with abstract ViewModel class works with subclass`() {
        abstract class AbstractViewModel : ViewModel() {
            abstract fun getValue(): String
        }

        class ConcreteViewModel(private val value: String) : AbstractViewModel() {
            override fun getValue(): String = value
        }

        val expectedViewModel = ConcreteViewModel("concrete")
        val abstractFactory = GenericViewModelFactory(AbstractViewModel::class.java) { expectedViewModel }
        val createdViewModel = abstractFactory.create(AbstractViewModel::class.java)

        assertEquals("Abstract ViewModel subclass should be created", "concrete", (createdViewModel as ConcreteViewModel).getValue())
    }

    @Test
    fun `create with ViewModel subclass using isAssignableFrom`() {
        class ParentViewModel : ViewModel()
        class ChildViewModel : ParentViewModel()

        val childViewModel = ChildViewModel()
        val childFactory = GenericViewModelFactory(ParentViewModel::class.java) { childViewModel }
        val createdViewModel = childFactory.create(ChildViewModel::class.java)

        assertSame("Child ViewModel should be assignable from parent", childViewModel, createdViewModel)
    }

    @Test
    fun `create with wrong class name in exception message`() {
        class WrongViewModel : ViewModel()

        val exception = try {
            factory.create(WrongViewModel::class.java)
            null
        } catch (e: IllegalArgumentException) {
            e
        }

        assertNotNull("Exception should be thrown", exception)
        assertTrue("Message should contain 'Unknown ViewModel class'", exception?.message?.contains("Unknown ViewModel class") ?: false)
        assertTrue("Message should contain class name", exception?.message?.contains("WrongViewModel") ?: false)
    }

    @Test
    fun `factory implements ViewModelProvider Factory interface`() {
        assertTrue("GenericViewModelFactory should implement ViewModelProvider.Factory", factory is ViewModelProvider.Factory)
    }

    @Test
    fun `create handles ViewModel with default values`() {
        class DefaultViewModel(val value: Int = 42) : ViewModel()

        val expectedViewModel = DefaultViewModel()
        val defaultFactory = GenericViewModelFactory(DefaultViewModel::class.java) { expectedViewModel }
        val createdViewModel = defaultFactory.create(DefaultViewModel::class.java)

        assertEquals("Default value should be preserved", 42, (createdViewModel as DefaultViewModel).value)
    }

    @Test
    fun `create maintains ViewModel state across multiple creations`() {
        testViewModel.value = "state1"

        val viewModel1 = factory.create(SampleViewModel::class.java)
        (viewModel1 as SampleViewModel).value = "state2"

        val viewModel2 = factory.create(SampleViewModel::class.java)

        assertEquals("ViewModel state should be maintained across creations", "state2", (viewModel2 as SampleViewModel).value)
    }

    @Test
    fun `create handles ViewModel with list parameters`() {
        class ListViewModel(val items: List<String>) : ViewModel()

        val expectedViewModel = ListViewModel(listOf("a", "b", "c"))
        val listFactory = GenericViewModelFactory(ListViewModel::class.java) { expectedViewModel }
        val createdViewModel = listFactory.create(ListViewModel::class.java)

        assertEquals("List parameter should be preserved", listOf("a", "b", "c"), (createdViewModel as ListViewModel).items)
    }

    @Test
    fun `create handles ViewModel with nested data classes`() {
        data class NestedData(val value: String, val count: Int)
        class NestedViewModel(val data: NestedData) : ViewModel()

        val expectedViewModel = NestedViewModel(NestedData("nested", 10))
        val nestedFactory = GenericViewModelFactory(NestedViewModel::class.java) { expectedViewModel }
        val createdViewModel = nestedFactory.create(NestedViewModel::class.java)

        assertEquals("Nested data should be preserved", "nested", (createdViewModel as NestedViewModel).data.value)
        assertEquals("Nested count should be preserved", 10, (createdViewModel as NestedViewModel).data.count)
    }

    @Test
    fun `create handles ViewModel initialization side effects`() {
        var initCount = 0

        class SideEffectViewModel : ViewModel() {
            init {
                initCount++
            }
            val count: Int get() = initCount
        }

        val sideEffectViewModel = SideEffectViewModel()
        val sideEffectFactory = GenericViewModelFactory(SideEffectViewModel::class.java) { sideEffectViewModel }

        assertEquals("Init should not be called during factory creation", 1, sideEffectViewModel.count)

        val createdViewModel = sideEffectFactory.create(SideEffectViewModel::class.java)

        assertEquals("Init should still only be called once", 1, (createdViewModel as SideEffectViewModel).count)
    }

    @Test
    fun `create throws exception for null creator`() {
        try {
            GenericViewModelFactory(SampleViewModel::class.java, null)
            fail("Expected IllegalArgumentException for null creator")
        } catch (e: Exception) {
            assertTrue("Should throw exception for null creator", e is IllegalArgumentException || e is NullPointerException)
        }
    }

    @Test
    fun `create returns ViewModel that survives configuration changes`() {
        testViewModel.value = "persistent"

        val viewModel1 = factory.create(SampleViewModel::class.java)

        testViewModel.value = "modified"

        val viewModel2 = factory.create(SampleViewModel::class.java)

        assertSame("Same instance should be returned", viewModel1, viewModel2)
        assertEquals("ViewModel should maintain state", "modified", (viewModel2 as SampleViewModel).value)
    }
}
