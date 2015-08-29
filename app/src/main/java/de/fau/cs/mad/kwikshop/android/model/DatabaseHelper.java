package de.fau.cs.mad.kwikshop.android.model;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.util.Log;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.fau.cs.mad.kwikshop.android.R;
import de.fau.cs.mad.kwikshop.common.AccountID;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionData;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.RepeatType;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;
import de.fau.cs.mad.kwikshop.common.localization.ResourceId;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper{

    private final Context context;

    //fields containing mapping information required for upgrade of resource ids (version 28)
    private static boolean resourceMappingInitialized = false;
    private static final Map<String, ResourceId> migrationResourceMapping = new HashMap<>();


    private static final String DATABASE_NAME = "kwikshop.db";

    //note if you increment here, also add migration strategy with correct version to onUpgrade
    private static final int DATABASE_VERSION = 44; //increment every time you change the database model

    private Dao<Item, Integer> itemDao = null;
    private RuntimeExceptionDao<Item, Integer> itemRuntimeDao = null;

    private Dao<AccountID, Integer> accountIDDao = null;
    private RuntimeExceptionDao<AccountID, Integer> accountIDRuntimeDao = null;

    private Dao<ShoppingList, Integer> shoppingListDao = null;
    private RuntimeExceptionDao<ShoppingList, Integer> shoppingListRuntimeDao = null;

    private Dao<Unit, Integer> unitDao = null;
    private RuntimeExceptionDao<Unit, Integer> unitRuntimeDao = null;

    private Dao<Group, Integer> groupDao = null;
    private RuntimeExceptionDao<Group, Integer> groupRuntimeDao = null;

    private Dao<CalendarEventDate, Integer> calendarDao = null;
    private RuntimeExceptionDao<CalendarEventDate, Integer> calendarRuntimeDao = null;

    private Dao<AutoCompletionData, Integer> autoCompletionDao = null;
    private RuntimeExceptionDao<AutoCompletionData, Integer> autoCompletionRuntimeDao = null;

    private Dao<AutoCompletionBrandData, Integer> autoCompletionBrandDao = null;
    private RuntimeExceptionDao<AutoCompletionBrandData, Integer> autoCompletionBrandRuntimeDao = null;

    private Dao<Recipe, Integer> recipeDao = null;
    private RuntimeExceptionDao<Recipe, Integer> recipeRunTimeDao = null;

    private Dao<LastLocation, Integer> locationDao = null;
    private RuntimeExceptionDao<LastLocation, Integer> locationRunTimeDao = null;

    private Dao<DeletedList, Integer> deletedListDao = null;
    private RuntimeExceptionDao<DeletedList, Integer> deletedListRuntimeDao = null;

    private Dao<DeletedItem, Integer> deletedItemDao = null;
    private RuntimeExceptionDao<DeletedItem, Integer> deletedItemRuntimeDao = null;


    @Inject
    public DatabaseHelper(Context context)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Item.class);
            TableUtils.createTable(connectionSource, AccountID.class);
            TableUtils.createTable(connectionSource, ShoppingList.class);
            TableUtils.createTable(connectionSource, Unit.class);
            TableUtils.createTable(connectionSource, Group.class);
            TableUtils.createTable(connectionSource, CalendarEventDate.class);
            TableUtils.createTable(connectionSource, AutoCompletionData.class);
            TableUtils.createTable(connectionSource, LastLocation.class);
            TableUtils.createTable(connectionSource, AutoCompletionBrandData.class);
            TableUtils.createTable(connectionSource, Recipe.class);
            TableUtils.createTable(connectionSource, DeletedItem.class);
            TableUtils.createTable(connectionSource, DeletedList.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {


        //todo put try catch around the if cases?
        //version of the play store release
        if(oldVersion == 7){
            try {
                //Changes in CalendarEventDate were to big to migrate them
                TableUtils.dropTable(connectionSource, CalendarEventDate.class, true);
                TableUtils.createTable(connectionSource, CalendarEventDate.class);
                //Recipes are completely new
                TableUtils.createTable(connectionSource, Recipe.class);
                //Item changes
                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN recipe RECIPE;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN lastBought DATE;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN regularlyRepeatItem BOOLEAN;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN periodType TIMEPERIODSENUM;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN selectedRepeatTime INTEGER;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN remindFromNextPurchaseOn BOOLEAN;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN remindAtDate DATE;");
                //AutoCompletionData changes
                autoCompletionDao = ListStorageFragment.getDatabaseHelper().getAutoCompletionDao();
                autoCompletionDao.executeRaw("ALTER TABLE 'autoCompletionData' ADD COLUMN group GROUP;");

            }catch (SQLException e){
                e.printStackTrace();
            }
        }

        if(oldVersion < 19){
            try {
                recipeDao = ListStorageFragment.getDatabaseHelper().getRecipeDao();
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN scaleFactor INTEGER;");
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN scaleName STRING;");
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        if(oldVersion < 20){
            try {
                TableUtils.createTable(connectionSource,LastLocation.class);
                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN location LASTLOCATION;");

            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        if(oldVersion < 21){
            try {
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN lastModifiedDate TIMESTAMP;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 22) {
            try {
                shoppingListDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN lastModifiedDate TIMESTAMP;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 23) {
            //add next upgrade step here
            try {
                TableUtils.createTable(connectionSource,LastLocation.class);
                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN location LASTLOCATION;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(oldVersion < 24){
            try {
                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' DROP COLUMN location;");
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN location_id LASTLOCATION;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(oldVersion < 25){
            try {
                //Item changes
                itemDao =  ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN imageItem VARBYTEARRAY;");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        if(oldVersion < 26){
            try {
                //Item changes
                itemDao =  ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' DROP COLUMN imageItem;" );
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN imageItem STRING;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 27){
            try {
                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'location' ADD COLUMN accuracy;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 28) {

            //upgrade how resource ids are stored
            // before v28, the name of the Android resource was stored
            // beginning with v28, the enum ResourceId is used instead.
            // this updates the table so it includes a ResourceId

            try {
                groupDao = ListStorageFragment.getDatabaseHelper().getGroupDao();

                groupDao.executeRaw("ALTER TABLE 'group' ADD COLUMN resourceId;");

                GenericRawResults<String[]> rawResults = groupDao.queryRaw("SELECT DISTINCT displayNameResourceName " +
                        "FROM 'group' WHERE displayNameResourceName != '' AND displayNameResourceName IS NOT NULL;");
                for(String[] row : rawResults) {


                    String statement = String.format(
                            "UPDATE 'group' SET resourceId = '%s' WHERE displayNameResourceName = '%s';",
                            getResourceId(row[0]).toString(),
                            row[0]);

                    groupDao.executeRaw(statement);
                }

                //Sqlite does not seem to support dropping columns, so the column must just stay there...
                //groupDao.executeRaw("ALTER TABLE 'group' DROP COLUMN displayNameResourceName;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 29) {


            //upgrade how resource ids are stored
            // before v29, the name of the Android resource was stored
            // beginning with v29, the enum ResourceId is used instead.
            // this updates the table so it includes a ResourceId
            try {
                unitDao = ListStorageFragment.getDatabaseHelper().getUnitDao();

                unitDao.executeRaw("ALTER TABLE 'unit' ADD COLUMN resourceId;");
                unitDao.executeRaw("ALTER TABLE 'unit' ADD COLUMN shortNameResourceId;");

                GenericRawResults<String[]> rawResults = unitDao.queryRaw("SELECT DISTINCT displayNameResourceName " +
                        "FROM 'unit' WHERE displayNameResourceName != '' AND displayNameResourceName IS NOT NULL;");
                for(String[] row : rawResults) {

                    String statement = String.format(
                            "UPDATE 'unit' SET resourceId = '%s' WHERE displayNameResourceName = '%s';",
                            getResourceId(row[0]).toString(),
                            row[0]);

                    unitDao.executeRaw(statement);
                }


                GenericRawResults<String[]> shortNameRawResults = unitDao.queryRaw("SELECT DISTINCT shortDisplayNameResourceName " +
                        "FROM 'unit' WHERE shortDisplayNameResourceName != '' AND shortDisplayNameResourceName IS NOT NULL;");

                for(String[] row : shortNameRawResults) {

                    String statement = String.format(
                            "UPDATE 'unit' SET shortNameResourceId = '%s' WHERE shortDisplayNameResourceName = '%s';",
                            getResourceId(row[0]).toString(),
                            row[0]);

                    unitDao.executeRaw(statement);
                }

                //Sqlite does not seem to support dropping columns, so the two redundant column smust just stay there...


            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        if(oldVersion < 30) {
            try {

                itemDao =  ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN repeatType;");

                String template = "UPDATE 'item' SET repeatType = '%s' WHERE regularlyRepeatItem = %s;";
                itemDao.executeRaw(String.format(template, RepeatType.None, 0));
                itemDao.executeRaw(String.format(template, RepeatType.Schedule, 1));

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 31) {
            try {

                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN serverId INTEGER;");

                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN serverId INTEGER;");

                recipeDao = ListStorageFragment.getDatabaseHelper().getRecipeDao();
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN serverId INTEGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 32) {
            try {

                groupDao = ListStorageFragment.getDatabaseHelper().getGroupDao();
                groupDao.executeRaw("ALTER TABLE 'group' ADD COLUMN serverId INTEGER;");

                locationDao = ListStorageFragment.getDatabaseHelper().getLocationDao();
                locationDao.executeRaw("ALTER TABLE 'location' ADD COLUMN serverId INTEGER;");

                unitDao = ListStorageFragment.getDatabaseHelper().getUnitDao();
                unitDao.executeRaw("ALTER TABLE 'unit' ADD COLUMN serverId INTEGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 33) {
            try {

                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN version INTEGER;");

                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN serverVersion INTEGER;");

                recipeDao = ListStorageFragment.getDatabaseHelper().getRecipeDao();
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN serverVersion INTEGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 34) {
            try {

                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN deleted BOOLEAN;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 35) {

            // add field 'modifiedSinceLastSync' to Item, ShoppingList and Recipe
            try {

                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN modifiedSinceLastSync BOOLEAN;");

                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN modifiedSinceLastSync BOOLEAN;");

                recipeDao = ListStorageFragment.getDatabaseHelper().getRecipeDao();
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN modifiedSinceLastSync BOOLEAN;");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


        if(oldVersion < 36) {

            try {

                deletedListDao = ListStorageFragment.getDatabaseHelper().getDeletedListDao();
                deletedListDao.executeRaw("ALTER TABLE 'deletedList' ADD COLUMN listIdServer INTEGER;");

                deletedItemDao = ListStorageFragment.getDatabaseHelper().getDeletedItemDao();
                deletedItemDao.executeRaw("ALTER TABLE 'deletedItem' ADD COLUMN listIdServer INTEGER;");
                deletedItemDao.executeRaw("ALTER TABLE 'deletedItem' ADD COLUMN itemIdServer INTEGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        //TODO migration 36 -> 37

        if(oldVersion < 38) {

         try {
             TableUtils.createTable(connectionSource, DeletedItem.class);
             TableUtils.createTable(connectionSource, DeletedList.class);

         } catch (SQLException e) {
             e.printStackTrace();
         }

        }

        if(oldVersion < 39) {
            try {
                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN ownerId STRING;");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }


        if(oldVersion < 40) {
            try {
                shoppingListDao = ListStorageFragment.getDatabaseHelper().getShoppingListDao();
                shoppingListDao.executeRaw("ALTER TABLE 'shoppingList' ADD COLUMN predefinedId INTERGER;");

                recipeDao = ListStorageFragment.getDatabaseHelper().getRecipeDao();
                recipeDao.executeRaw("ALTER TABLE 'recipe' ADD COLUMN predefinedId INTERGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if(oldVersion < 41) {

            try {
                itemDao = ListStorageFragment.getDatabaseHelper().getItemDao();
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN predefinedId INTERGER;");

                groupDao = ListStorageFragment.getDatabaseHelper().getGroupDao();
                groupDao.executeRaw("ALTER TABLE 'group' ADD COLUMN predefinedId INTERGER;");

                unitDao = ListStorageFragment.getDatabaseHelper().getUnitDao();
                unitDao.executeRaw("ALTER TABLE 'unit' ADD COLUMN predefinedId INTERGER;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 42) {

            try {
                assignPredefinedGroupIds();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                assignPredefinedUnitIds();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(oldVersion < 43){
            //nothing to do
        }

        if(oldVersion < 44) {

            try {
                locationDao = getLocationDao();

                locationDao.executeRaw("ALTER TABLE 'location' ADD COLUMN placeId STRING;");

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public Dao<Item, Integer> getItemDao() throws SQLException {
        if (itemDao == null) {
            itemDao = getDao(Item.class);
        }
        return itemDao;
    }

    public RuntimeExceptionDao<Item, Integer> getItemRuntimeDao() {
        if (itemRuntimeDao == null) {
            itemRuntimeDao = getRuntimeExceptionDao(Item.class);
        }
        return itemRuntimeDao;
    }


    public Dao<AccountID, Integer> getAccountIDDao() throws SQLException {
        if (accountIDDao == null) {
            accountIDDao = getDao(AccountID.class);
        }
        return accountIDDao;
    }

    public RuntimeExceptionDao<AccountID, Integer> getAccountIDRuntimeDao() {
        if (accountIDRuntimeDao == null) {
            accountIDRuntimeDao = getRuntimeExceptionDao(AccountID.class);
        }
        return accountIDRuntimeDao;
    }

    public Dao<ShoppingList, Integer> getShoppingListDao() throws SQLException {
        if (shoppingListDao == null) {
            shoppingListDao = getDao(ShoppingList.class);
        }
        return shoppingListDao;
    }

    public RuntimeExceptionDao<ShoppingList, Integer> getShoppingListRuntimeDao() {
        if (shoppingListRuntimeDao == null) {
            shoppingListRuntimeDao = getRuntimeExceptionDao(ShoppingList.class);
        }
        return shoppingListRuntimeDao;
    }

    public Dao<Unit, Integer> getUnitDao() throws SQLException {
        if (unitDao == null) {
            unitDao = getDao(Unit.class);
        }
        return unitDao;
    }

    public RuntimeExceptionDao<Unit, Integer> getUnitRuntimeDao() {
        if (unitRuntimeDao == null) {
            unitRuntimeDao = getRuntimeExceptionDao(Unit.class);
        }
        return unitRuntimeDao;
    }

    public Dao<Group, Integer> getGroupDao() throws SQLException {
        if (groupDao == null) {
            groupDao = getDao(Group.class);
        }
        return groupDao;
    }

    public RuntimeExceptionDao<Group, Integer> getGroupRuntimeDao() {
        if (groupRuntimeDao == null) {
            groupRuntimeDao = getRuntimeExceptionDao(Group.class);
        }
        return groupRuntimeDao;
    }

    public Dao<CalendarEventDate, Integer> getCalendarDao() throws SQLException {
        if (calendarDao == null) {
            calendarDao = getDao(CalendarEventDate.class);
        }
        return calendarDao;
    }

    public RuntimeExceptionDao<CalendarEventDate, Integer> getCalendarRuntimeDao() {
        if (calendarRuntimeDao == null) {
            calendarRuntimeDao = getRuntimeExceptionDao(CalendarEventDate.class);
        }
        return calendarRuntimeDao;
    }

    public Dao<AutoCompletionData, Integer> getAutoCompletionDao() throws SQLException{
        if (autoCompletionDao == null) {
            autoCompletionDao = getDao(AutoCompletionData.class);
        }
        return autoCompletionDao;
    }

    public RuntimeExceptionDao<AutoCompletionData, Integer> getAutoCompletionRuntimeDao () {
        if (autoCompletionRuntimeDao == null) {
            autoCompletionRuntimeDao = getRuntimeExceptionDao(AutoCompletionData.class);
        }
        return autoCompletionRuntimeDao;
    }

    public Dao<AutoCompletionBrandData, Integer> getAutoCompletionBrandDao() throws SQLException{
        if (autoCompletionBrandDao == null) {
            autoCompletionBrandDao = getDao(AutoCompletionBrandData.class);
        }
        return autoCompletionBrandDao;
    }

    public RuntimeExceptionDao<AutoCompletionBrandData, Integer> getAutoCompletionBrandRuntimeDao () {
        if (autoCompletionBrandRuntimeDao == null) {
            autoCompletionBrandRuntimeDao = getRuntimeExceptionDao(AutoCompletionBrandData.class);
        }
        return autoCompletionBrandRuntimeDao;
    }

    public Dao<Recipe, Integer> getRecipeDao() throws SQLException {
        if (recipeDao == null) {
            recipeDao = getDao(Recipe.class);
        }
        return recipeDao;
    }

    public RuntimeExceptionDao<Recipe, Integer> getRecipeRuntimeDao() {
        if (recipeRunTimeDao == null) {
            recipeRunTimeDao = getRuntimeExceptionDao(Recipe.class);
        }
        return recipeRunTimeDao;
    }

    public Dao<LastLocation, Integer> getLocationDao() throws SQLException {
        if (locationDao == null) {
            locationDao = getDao(LastLocation.class);
        }
        return locationDao;
    }

    public RuntimeExceptionDao<LastLocation, Integer> getLocationRuntimeDao() {
        if (locationRunTimeDao == null) {
            locationRunTimeDao = getRuntimeExceptionDao(LastLocation.class);
        }
        return locationRunTimeDao;
    }

    public Dao<DeletedList,Integer> getDeletedListDao() throws SQLException {
        if (deletedListDao == null) {
            deletedListDao = getDao(DeletedList.class);
        }
        return deletedListDao;
    }

    public RuntimeExceptionDao<DeletedList, Integer> getDeletedListRuntimeDao() {
        if (deletedListRuntimeDao == null) {
            deletedListRuntimeDao = getRuntimeExceptionDao(DeletedList.class);
        }
        return deletedListRuntimeDao;
    }


    public Dao<DeletedItem, Integer> getDeletedItemDao() throws SQLException {
        if (deletedItemDao == null) {
            deletedItemDao = getDao(DeletedItem.class);
        }
        return deletedItemDao;
    }

    public RuntimeExceptionDao<DeletedItem, Integer> getDeletedItemRuntimeDao() {
        if (deletedItemRuntimeDao == null) {
            deletedItemRuntimeDao = getRuntimeExceptionDao(DeletedItem.class);
        }
        return deletedItemRuntimeDao;
    }

    @Override
    public void close() {
        super.close();
        itemDao = null;
        itemRuntimeDao = null;

        accountIDDao = null;
        accountIDRuntimeDao = null;

        shoppingListDao = null;
        shoppingListRuntimeDao = null;

        unitDao = null;
        unitRuntimeDao = null;

        groupDao = null;
        groupRuntimeDao = null;

        calendarDao = null;
        calendarRuntimeDao = null;

        autoCompletionDao = null;
        autoCompletionRuntimeDao = null;

        autoCompletionBrandDao = null;
        autoCompletionBrandRuntimeDao = null;

        recipeDao = null;
        recipeRunTimeDao = null;

        locationDao = null;
        locationRunTimeDao = null;

        //itemRepeatDao = null;
        //itemRepeatRunTimeDao = null;
    }


    /***
     * Gets the ResourceId for the specified Android resource name
     */
    private ResourceId getResourceId(String androidResourceName) {

        initializeMigrationResourceMapping(this.context);

        if(migrationResourceMapping.containsKey(androidResourceName)) {
            return migrationResourceMapping.get(androidResourceName);
        } else {
            throw new IllegalArgumentException("Unknown resource name: " + androidResourceName);
        }

    }

    /**
     * Populates the migrationResourceMapping map
     */
    private static synchronized void initializeMigrationResourceMapping(Context context) {

        if(resourceMappingInitialized) {
            return;
        }

        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_CoffeeAndTea), ResourceId.Group_CoffeeAndTea);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_healthAndHygiene), ResourceId.Group_HealthAndHygiene);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_petSupplies), ResourceId.Group_PetSupplies);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_household), ResourceId.Group_Household);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_breakPastries), ResourceId.Group_BreadAndPastries);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_beverages), ResourceId.Group_Beverages);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_sweetsAndSnacks), ResourceId.Group_SweetsAndSnacks);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_babyFoods), ResourceId.Group_BabyFoods);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_pasta), ResourceId.Group_Pasta);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_dairy), ResourceId.Group_Dairy);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_fruitsAndVegetables), ResourceId.Group_FruitsAndVegetables);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_meatAndFish), ResourceId.Group_MeatAndFish);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_ingredientsAndSpices), ResourceId.Group_IngredientsAndSpices);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_frozenAndConvenience), ResourceId.Group_FrozenAndConvenience);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_tobacco), ResourceId.Group_Tobacco);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.group_Other), ResourceId.Group_Other);

        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_piece), ResourceId.Unit_Piece);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_piece_short), ResourceId.Unit_short_Piece);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_bag), ResourceId.Unit_Bag);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_bottle), ResourceId.Unit_Bottle);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_box), ResourceId.Unit_Box);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_pack), ResourceId.Unit_Pack);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_dozen), ResourceId.Unit_Dozen);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_gram), ResourceId.Unit_Gram);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_gram_short), ResourceId.Unit_short_Gram );
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_kilogram), ResourceId.Unit_Kilogram);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_kilogram_short), ResourceId.Unit_short_Kilogram);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_millilitre), ResourceId.Unit_Millilitre);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_millilitre_short), ResourceId.Unit_short_Millilitre);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_litre), ResourceId.Unit_Litre );
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_litre_short), ResourceId.Unit_short_Litre);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_cup), ResourceId.Unit_Cup);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_tablespoon), ResourceId.Unit_Tablespoon);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_tablespoon_short), ResourceId.Unit_short_Tablespoon);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_can), ResourceId.Unit_Can);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_teaspoon), ResourceId.Unit_Teaspoon);
        migrationResourceMapping.put(context.getResources().getResourceName(R.string.unit_teaspoon_short), ResourceId.Unit_short_Teaspoon);

        resourceMappingInitialized = true;
    }


    private void assignPredefinedGroupIds() throws SQLException {

        Map<String, Group> predefinedGroups = new HashMap<>();

        for(Group g : new DefaultDataProvider().getPredefinedGroups()) {
            predefinedGroups.put(g.getName(), g);
        }

        List<Group> currentGroups = getGroupDao().queryForAll();
        for(Group g : currentGroups) {
            if(g.getResourceId() != null && predefinedGroups.containsKey(g.getName())) {
                g.setPredefinedId(predefinedGroups.get(g.getName()).getPredefinedId());
                getGroupDao().update(g);
            }
        }
    }

    private void assignPredefinedUnitIds() throws SQLException {

        Map<String, Unit> predefinedUnits = new HashMap<>();

        for(Unit u : new DefaultDataProvider().getPredefinedUnits()) {
            predefinedUnits.put(u.getName(), u);
        }

        List<Unit> currentUnits = getUnitDao().queryForAll();
        for(Unit u : currentUnits) {
            if(u.getResourceId() != null && predefinedUnits.containsKey(u.getName())) {
                u.setPredefinedId(predefinedUnits.get(u.getName()).getPredefinedId());
                getUnitDao().update(u);
            }
        }
    }

    /**
     * Loads all the fields of all the supplied items which unfortunately ORMLite does not do automatically
     */
    public void refreshItemsRecursively(Collection<Item> items) throws SQLException {

        for (Item i : items) {

            if (i.getUnit() != null) {
                getUnitDao().refresh(i.getUnit());
            }

            if (i.getGroup() != null) {
                getGroupDao().refresh(i.getGroup());
            }

            if(i.getLocation() != null) {
                getLocationDao().refresh(i.getLocation());
            }
        }

    }


}
