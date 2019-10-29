/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.gob.fire.logs;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A HashMap that is optimized for fast shallow copies. If the copy-ctor is
 * passed another FastCopyHashMap, or clone is called on this map, the shallow
 * copy can be performed using little more than a single array copy. In order to
 * accomplish this, immutable objects must be used internally, so update
 * operations result in slightly more object churn than <code>HashMap</code>.
 *
 * Note: It is very important to use a smaller load factor than you normally
 * would for HashMap, since the implementation is open-addressed with linear
 * probing. With a 50% load-factor a get is expected to return in only 2 probes.
 * However, a 90% load-factor is expected to return in around 50 probes.
 *
 * @author Jason T. Greene
 */
class FastCopyHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable
{
   /**
    * Marks null keys.
    */
   private static final Object NULL = new Object();

   /**
    * Serialization ID
    */
   private static final long serialVersionUID = 10929568968762L;

   /**
    * Same default as HashMap, must be a power of 2
    */
   private static final int DEFAULT_CAPACITY = 8;

   /**
    * MAX_INT - 1
    */
   private static final int MAXIMUM_CAPACITY = 1 << 30;

   /**
    * 67%, just like IdentityHashMap
    */
   private static final float DEFAULT_LOAD_FACTOR = 0.67f;

   /**
    * The open-addressed table
    */
   private transient Entry<K, V>[] table;

   /**
    * The current number of key-value pairs
    */
   private transient int size;

   /**
    * The next resize
    */
   private transient int threshold;

   /**
    * The user defined load factor which defines when to resize
    */
   private final float loadFactor;

   /**
    * Counter used to detect changes made outside of an iterator
    */
   private transient int modCount;

   // Cached views
   private transient KeySet keySet;
   private transient Values values;
   private transient EntrySet entrySet;

   public FastCopyHashMap(int initialCapacity, final float loadFactor)
   {
      if (initialCapacity < 0) {
		throw new IllegalArgumentException("Can not have a negative size table!");
	}

      if (initialCapacity > MAXIMUM_CAPACITY) {
		initialCapacity = MAXIMUM_CAPACITY;
	}

      if (!(loadFactor > 0F && loadFactor <= 1F)) {
		throw new IllegalArgumentException("Load factor must be greater than 0 and less than or equal to 1");
	}

      this.loadFactor = loadFactor;
      init(initialCapacity, loadFactor);
   }

   @SuppressWarnings("unchecked")
   public FastCopyHashMap(final Map<? extends K, ? extends V> map)
   {
      if (map instanceof FastCopyHashMap)
      {
         final FastCopyHashMap<? extends K, ? extends V> fast = (FastCopyHashMap<? extends K, ? extends V>) map;
         this.table = (Entry<K, V>[]) fast.table.clone();
         this.loadFactor = fast.loadFactor;
         this.size = fast.size;
         this.threshold = fast.threshold;
      }
      else
      {
         this.loadFactor = DEFAULT_LOAD_FACTOR;
         init(map.size(), this.loadFactor);
         putAll(map);
      }
   }

   @SuppressWarnings("unchecked")
   private void init(final int initialCapacity, final float loadFactor)
   {
      int c = 1;
      for (; c < initialCapacity; c <<= 1) {

	}
      this.threshold = (int) (c * loadFactor);

      // Include the load factor when sizing the table for the first time
      if (initialCapacity > this.threshold && c < MAXIMUM_CAPACITY)
      {
         c <<= 1;
         this.threshold = (int) (c * loadFactor);
      }

      this.table = new Entry[c];
   }

   public FastCopyHashMap(final int initialCapacity)
   {
      this(initialCapacity, DEFAULT_LOAD_FACTOR);
   }

   public FastCopyHashMap()
   {
      this(DEFAULT_CAPACITY);
   }

   // The normal bit spreader...
   private static final int hash(final Object key)
   {
      int h = key.hashCode();
      h ^= h >>> 20 ^ h >>> 12;
      return h ^ h >>> 7 ^ h >>> 4;
   }

   @SuppressWarnings("unchecked")
   private static final <K> K maskNull(final K key)
   {
      return key == null ? (K) NULL : key;
   }

   private static final <K> K unmaskNull(final K key)
   {
      return key == NULL ? null : key;
   }

   private int nextIndex(int index, final int length)
   {
      index = index >= length - 1 ? 0 : index + 1;
      return index;
   }

   private static final boolean eq(final Object o1, final Object o2)
   {
      return o1 == o2 || o1 != null && o1.equals(o2);
   }

   private static final int index(final int hashCode, final int length)
   {
      return hashCode & length - 1;
   }

   @Override
public int size()
   {
      return this.size;
   }

   @Override
public boolean isEmpty()
   {
      return this.size == 0;
   }

   @Override
public V get(Object key)
   {
      key = maskNull(key);

      final int hash = hash(key);
      final int length = this.table.length;
      int index = index(hash, length);

      for (final int start = index; ;)
      {
         final Entry<K, V> e = this.table[index];
         if (e == null) {
			return null;
		}

         if (e.hash == hash && eq(key, e.key)) {
			return e.value;
		}

         index = nextIndex(index, length);
         if (index == start) {
			return null;
		}
      }
   }

   @Override
public boolean containsKey(Object key)
   {
      key = maskNull(key);

      final int hash = hash(key);
      final int length = this.table.length;
      int index = index(hash, length);

      for (final int start = index; ;)
      {
         final Entry<K, V> e = this.table[index];
         if (e == null) {
			return false;
		}

         if (e.hash == hash && eq(key, e.key)) {
			return true;
		}

         index = nextIndex(index, length);
         if (index == start) {
			return false;
		}
      }
   }

   @Override
public boolean containsValue(final Object value)
   {
      for (final Entry<K, V> e : this.table) {
		if (e != null && eq(value, e.value)) {
			return true;
		}
	}

      return false;
   }

   @Override
public V put(K key, final V value)
   {
      key = maskNull(key);

      final Entry<K, V>[] table = this.table;
      final int hash = hash(key);
      final int length = table.length;
      int index = index(hash, length);

      for (final int start = index; ;)
      {
         final Entry<K, V> e = table[index];
         if (e == null) {
			break;
		}

         if (e.hash == hash && eq(key, e.key))
         {
            table[index] = new Entry<>(e.key, e.hash, value);
            return e.value;
         }

         index = nextIndex(index, length);
         if (index == start) {
			throw new IllegalStateException("Table is full!");
		}
      }

      this.modCount++;
      table[index] = new Entry<>(key, hash, value);
      if (++this.size >= this.threshold) {
		resize(length);
	}

      return null;
   }


   @SuppressWarnings("unchecked")
   private void resize(final int from)
   {
      final int newLength = from << 1;

      // Can't get any bigger
      if (newLength > MAXIMUM_CAPACITY || newLength <= from) {
		return;
	}

      final Entry<K, V>[] newTable = new Entry[newLength];
      final Entry<K, V>[] old = this.table;

      for (final Entry<K, V> e : old)
      {
         if (e == null) {
			continue;
		}

         int index = index(e.hash, newLength);
         while (newTable[index] != null) {
			index = nextIndex(index, newLength);
		}

         newTable[index] = e;
      }

      this.threshold = (int) (this.loadFactor * newLength);
      this.table = newTable;
   }

   @Override
public void putAll(final Map<? extends K, ? extends V> map)
   {
      int size = map.size();
      if (size == 0) {
		return;
	}

      if (size > this.threshold)
      {
         if (size > MAXIMUM_CAPACITY) {
			size = MAXIMUM_CAPACITY;
		}

         int length = this.table.length;
         for (; length < size; length <<= 1) {

		}

         resize(length);
      }

      for (final Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
		put(e.getKey(), e.getValue());
	}
   }

   @Override
public V remove(Object key)
   {
      key = maskNull(key);

      final Entry<K, V>[] table = this.table;
      final int length = table.length;
      final int hash = hash(key);
      final int start = index(hash, length);

      for (int index = start; ;)
      {
         final Entry<K, V> e = table[index];
         if (e == null) {
			return null;
		}

         if (e.hash == hash && eq(key, e.key))
         {
            table[index] = null;
            relocate(index);
            this.modCount++;
            this.size--;
            return e.value;
         }

         index = nextIndex(index, length);
         if (index == start) {
			return null;
		}
      }


   }

   private void relocate(int start)
   {
      final Entry<K, V>[] table = this.table;
      final int length = table.length;
      int current = nextIndex(start, length);

      for (; ;)
      {
         final Entry<K, V> e = table[current];
         if (e == null) {
			return;
		}

         // A Doug Lea variant of Knuth's Section 6.4 Algorithm R.
         // This provides a non-recursive method of relocating
         // entries to their optimal positions once a gap is created.
         final int prefer = index(e.hash, length);
         if (current < prefer && (prefer <= start || start <= current)
               || prefer <= start && start <= current)
         {
            table[start] = e;
            table[current] = null;
            start = current;
         }

         current = nextIndex(current, length);
      }
   }

   @Override
public void clear()
   {
      this.modCount++;
      final Entry<K, V>[] table = this.table;
      for (int i = 0; i < table.length; i++) {
		table[i] = null;
	}

      this.size = 0;
   }

   @Override
@SuppressWarnings("unchecked")
   public FastCopyHashMap<K, V> clone()
   {
      try
      {
         final FastCopyHashMap<K, V> clone = (FastCopyHashMap<K, V>) super.clone();
         clone.table = this.table.clone();
         clone.entrySet = null;
         clone.values = null;
         clone.keySet = null;
         return clone;
      }
      catch (final CloneNotSupportedException e)
      {
         // should never happen
         throw new IllegalStateException(e);
      }
   }

   public void printDebugStats()
   {
      int optimal = 0;
      int total = 0;
      int totalSkew = 0;
      int maxSkew = 0;
      for (int i = 0; i < this.table.length; i++)
      {
         final Entry<K, V> e = this.table[i];
         if (e != null)
         {

            total++;
            final int target = index(e.hash, this.table.length);
            if (i == target) {
				optimal++;
			} else
            {
               final int skew = Math.abs(i - target);
               if (skew > maxSkew) {
				maxSkew = skew;
			}
               totalSkew += skew;
            }

         }
      }
   }

   @Override
public Set<Map.Entry<K, V>> entrySet()
   {
      if (this.entrySet == null) {
		this.entrySet = new EntrySet();
	}

      return this.entrySet;
   }

   @Override
public Set<K> keySet()
   {
      if (this.keySet == null) {
		this.keySet = new KeySet();
	}

      return this.keySet;
   }

   @Override
public Collection<V> values()
   {
      if (this.values == null) {
		this.values = new Values();
	}

      return this.values;
   }

   @SuppressWarnings("unchecked")
   private void readObject(final java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
   {
      s.defaultReadObject();

      final int size = s.readInt();

      init(size, this.loadFactor);

      for (int i = 0; i < size; i++)
      {
         final K key = (K) s.readObject();
         final V value = (V) s.readObject();
         putForCreate(key, value);
      }

      this.size = size;
   }

   @SuppressWarnings("unchecked")
   private void putForCreate(K key, final V value)
   {
      key = maskNull(key);

      final Entry<K, V>[] table = this.table;
      final int hash = hash(key);
      final int length = table.length;
      int index = index(hash, length);

      Entry<K, V> e = table[index];
      while (e != null)
      {
         index = nextIndex(index, length);
         e = table[index];
      }

      table[index] = new Entry<>(key, hash, value);
   }

   private void writeObject(final java.io.ObjectOutputStream s) throws IOException
   {
      s.defaultWriteObject();
      s.writeInt(this.size);

      for (final Entry<K, V> e : this.table)
      {
         if (e != null)
         {
            s.writeObject(unmaskNull(e.key));
            s.writeObject(e.value);
         }
      }
   }

   private static final class Entry<K, V>
   {
      final K key;
      final int hash;
      final V value;

      Entry(final K key, final int hash, final V value)
      {
         this.key = key;
         this.hash = hash;
         this.value = value;
      }
   }

   private abstract class FastCopyHashMapIterator<E> implements Iterator<E>
   {
      private int next = 0;
      private int expectedCount = FastCopyHashMap.this.modCount;
      private int current = -1;
      private boolean hasNext;
      Entry<K, V> table[] = FastCopyHashMap.this.table;

      @Override
	public boolean hasNext()
      {
         if (this.hasNext == true) {
			return true;
		}

         final Entry<K, V> table[] = this.table;
         for (int i = this.next; i < table.length; i++)
         {
            if (table[i] != null)
            {
               this.next = i;
               return this.hasNext = true;
            }
         }

         this.next = table.length;
         return false;
      }

      protected Entry<K, V> nextEntry()
      {
         if (FastCopyHashMap.this.modCount != this.expectedCount) {
			throw new ConcurrentModificationException();
		}

         if (!this.hasNext && !hasNext()) {
			throw new NoSuchElementException();
		}

         this.current = this.next++;
         this.hasNext = false;

         return this.table[this.current];
      }

      @Override
	@SuppressWarnings("unchecked")
      public void remove()
      {
         if (FastCopyHashMap.this.modCount != this.expectedCount) {
			throw new ConcurrentModificationException();
		}

         final int current = this.current;
         int delete = current;

         if (current == -1) {
			throw new IllegalStateException();
		}

         // Invalidate current (prevents multiple remove)
         this.current = -1;

         // Start were we relocate
         this.next = delete;

         final Entry<K, V>[] table = this.table;
         if (table != FastCopyHashMap.this.table)
         {
            FastCopyHashMap.this.remove(table[delete].key);
            table[delete] = null;
            this.expectedCount = FastCopyHashMap.this.modCount;
            return;
         }


         final int length = table.length;
         int i = delete;

         table[delete] = null;
         FastCopyHashMap.this.size--;

         for (; ;)
         {
            i = nextIndex(i, length);
            final Entry<K, V> e = table[i];
            if (e == null) {
				break;
			}

            final int prefer = index(e.hash, length);
            if (i < prefer && (prefer <= delete || delete <= i)
                  || prefer <= delete && delete <= i)
            {
               // Snapshot the unseen portion of the table if we have
               // to relocate an entry that was already seen by this iterator
               if (i < current && current <= delete && table == FastCopyHashMap.this.table)
               {
                  final int remaining = length - current;
                  final Entry<K, V>[] newTable = new Entry[remaining];
                  System.arraycopy(table, current, newTable, 0, remaining);

                  // Replace iterator's table.
                  // Leave table local var pointing to the real table
                  this.table = newTable;
                  this.next = 0;
               }

               // Do the swap on the real table
               table[delete] = e;
               table[i] = null;
               delete = i;
            }
         }
      }
   }


   private class KeyIterator extends FastCopyHashMapIterator<K>
   {
      @Override
	public K next()
      {
         return unmaskNull(nextEntry().key);
      }
   }

   private class ValueIterator extends FastCopyHashMapIterator<V>
   {
      @Override
	public V next()
      {
         return nextEntry().value;
      }
   }

   private class EntryIterator extends FastCopyHashMapIterator<Map.Entry<K, V>>
   {
      private class WriteThroughEntry extends SimpleEntry<K, V>
      {
         WriteThroughEntry(final K key, final V value)
         {
            super(key, value);
         }

         @Override
		public V setValue(final V value)
         {
            if (EntryIterator.this.table != FastCopyHashMap.this.table) {
				FastCopyHashMap.this.put(getKey(), value);
			}

            return super.setValue(value);
         }
      }

      @Override
	public Map.Entry<K, V> next()
      {
         final Entry<K, V> e = nextEntry();
         return new WriteThroughEntry(unmaskNull(e.key), e.value);
      }

   }

   private class KeySet extends AbstractSet<K>
   {
      @Override
	public Iterator<K> iterator()
      {
         return new KeyIterator();
      }

      @Override
	public void clear()
      {
         FastCopyHashMap.this.clear();
      }

      @Override
	public boolean contains(final Object o)
      {
         return containsKey(o);
      }

      @Override
	public boolean remove(final Object o)
      {
         final int size = size();
         FastCopyHashMap.this.remove(o);
         return size() < size;
      }

      @Override
	public int size()
      {
         return FastCopyHashMap.this.size();
      }
   }

   private class Values extends AbstractCollection<V>
   {
      @Override
	public Iterator<V> iterator()
      {
         return new ValueIterator();
      }

      @Override
	public void clear()
      {
         FastCopyHashMap.this.clear();
      }

      @Override
	public int size()
      {
         return FastCopyHashMap.this.size();
      }
   }

   private class EntrySet extends AbstractSet<Map.Entry<K, V>>
   {
      @Override
	public Iterator<Map.Entry<K, V>> iterator()
      {
         return new EntryIterator();
      }

      @Override
	public boolean contains(final Object o)
      {
         if (!(o instanceof Map.Entry)) {
			return false;
		}

         final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
         final Object value = get(entry.getKey());
         return eq(entry.getValue(), value);
      }

      @Override
	public void clear()
      {
         FastCopyHashMap.this.clear();
      }

      @Override
	public boolean isEmpty()
      {
         return FastCopyHashMap.this.isEmpty();
      }

      @Override
	public int size()
      {
         return FastCopyHashMap.this.size();
      }
   }

   protected static class SimpleEntry<K, V> implements Map.Entry<K, V>
   {
      private final K key;
      private V value;

      SimpleEntry(final K key, final V value)
      {
         this.key = key;
         this.value = value;
      }

      SimpleEntry(final Map.Entry<K, V> entry)
      {
         this.key = entry.getKey();
         this.value = entry.getValue();
      }

      @Override
	public K getKey()
      {
         return this.key;
      }

      @Override
	public V getValue()
      {
         return this.value;
      }

      @Override
	public V setValue(final V value)
      {
         final V old = this.value;
         this.value = value;
         return old;
      }

      @Override
	public boolean equals(final Object o)
      {
         if (this == o) {
			return true;
		}

         if (!(o instanceof Map.Entry)) {
			return false;
		}
         final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
         return eq(this.key, e.getKey()) && eq(this.value, e.getValue());
      }

      @Override
	public int hashCode()
      {
         return (this.key == null ? 0 : hash(this.key)) ^
                (this.value == null ? 0 : hash(this.value));
      }

      @Override
	public String toString()
      {
         return getKey() + "=" + getValue();
      }
   }
}
