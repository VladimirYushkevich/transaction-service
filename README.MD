### txn-service
The core idea for this task is just to split 60 sec window (is configured
in *application.yml* file) into list of buckets. Then any transaction can be stored in corresponding bucket according to timestamp.
In the bucket there is a linked list to handle collisions.
Technically we have to handle following problems. For simplicity, we cache buckets with 5 s window and 1 s step:
* **Expiration**  
Transaction **A** has timestamp between 0 and 1 s  
Transaction **B** has timestamp between 2 and 3 s
Here is how the cache looks like:
```
| A |  | B |  |  |
```
When we need to write a Transaction **C** with timestamp between 5 and 6 s, we have to do two things. 
First we have to copy existing array to new one (left shift), taking into account expiration:
```
|  | B |  |  |  |
```
And calculate new bucket index (`shiftedIndex`) because initial one is out of array boundaries.
`shiftedIndex==0`
```
| C | B |  |  |  |
```
* **Out of order write** (when new transaction has a timestamp earlier than last stored one)  
Transaction **A** has timestamp between 3 and 4 s and should be written in very first bucket
```
| A |  |  |  |  |
```  
Then we have a new Transaction **B** with timestamp between 1 and 2 s. Since **B** is still in desired window
we have to do the same operations like for expiration but shifting in opposite direction (right):
```
|  |  | A |  |  |
```  
And insert `shiftedIndex==0` (always to first bucket for delayed transactions)
```
| B |  | A |  |  |
``` 
* **Reading after expiration**
During read at every moment of time we get a shifted copy.