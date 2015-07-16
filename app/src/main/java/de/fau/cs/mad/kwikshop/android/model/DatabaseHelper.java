package de.fau.cs.mad.kwikshop.android.model;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.util.Log;

import java.sql.SQLException;

import de.fau.cs.mad.kwikshop.common.AccountID;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionBrandData;
import de.fau.cs.mad.kwikshop.android.common.AutoCompletionData;
import de.fau.cs.mad.kwikshop.common.CalendarEventDate;
import de.fau.cs.mad.kwikshop.common.Group;
import de.fau.cs.mad.kwikshop.common.Item;
import de.fau.cs.mad.kwikshop.common.LastLocation;
import de.fau.cs.mad.kwikshop.common.Recipe;
import de.fau.cs.mad.kwikshop.common.ShoppingList;
import de.fau.cs.mad.kwikshop.common.Unit;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper{

    private static final String DATABASE_NAME = "kwikshop.db";

    //note if you increment here, also add migration strategy with correct version to onUpgrade
    private static final int DATABASE_VERSION = 25; //increment every time you change the database model

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

    public DatabaseHelper(Context context)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                itemDao.executeRaw("ALTER TABLE 'item' ADD COLUMN imageItem VARBINARY;");
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
}
