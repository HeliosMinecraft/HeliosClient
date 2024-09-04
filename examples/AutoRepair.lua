local threshold = 0.2  -- Durability percentage threshold to trigger repair (0.2 == 20% of max damage)
local repairing = false -- Flag to track if repair is ongoing

-- Function to check if an item has the Mending enchantment
function hasMendingEnchantment(itemStack)
    -- itemStack:getEnchantments() returns a NbtList which is a modified version of java List class.
    -- should work with almost everything.
    local enchantments = itemStack:getEnchantments() -- Get the list of enchantments on the item
    for i = 0, enchantments:size() - 1 do
        local enchantment = enchantments:getCompound(i) -- Get each enchantment as an NbtCompound
        local id = enchantment:getString("id") -- Get the ID of the enchantment
        if id == "minecraft:mending" then -- Check if the enchantment is Mending
            return true -- Return true if Mending is found
        end
    end
    return false -- Return false if Mending is not found
end

-- Function to automatically repair items
function autoRepair()
    if not mc or not mc.player then
        return -- Ensure Minecraft is fully initialized
    end
    local needsRepair = false

    -- Check items in hand and armor slots
    local itemsToCheck = {
        mc.player:getMainHandStack(), -- Main hand item
        mc.player:getOffHandStack(), -- Off hand item
        mc.player:getInventory():getArmorStack(0), -- Helmet
        mc.player:getInventory():getArmorStack(1), -- ChestPlate
        mc.player:getInventory():getArmorStack(2), -- Leggings
        mc.player:getInventory():getArmorStack(3)  -- Boots
    }

    -- Loop through each item to check if it needs repair
    for _, itemStack in ipairs(itemsToCheck) do
        --Damage is opposite of durability. So we want to check if the item has more damage than the threshold % of the max damage it can take.
        if not (itemStack == nil) and not (itemStack:isEmpty()) and itemStack:isDamageable() and itemStack:getDamage() >= (itemStack:getMaxDamage() * threshold) then
            if hasMendingEnchantment(itemStack) then
                needsRepair = true
                chatLib.sendHeliosMsg("Repairing " .. itemStack:getName():getString() .. " Damage: " .. itemStack:getDamage())
                break -- Exit the loop once an item needing repair is found
            end
        end
    end

    -- Make sure swapback is on in ExpThrower.

    -- Use the moduleManager to get the module by its name
    -- Get the ExpThrower module and cast it to ExpThrower
    -- ModuleManager.getModuleByName() returns the abstract Module_ and not ExpThrower itself so we need to cast it.
    local module_ = moduleManager:getModuleByName("ExpThrower")
    local expThrower = luajava.cast(luajava.bindClass(" dev.heliosclient.module.modules.player.ExpThrower"), module_)

    -- Uncomment this line to auto change the swapback setting to true.
    expThrower.swapBack.value = true

    if needsRepair and not repairing then
        -- Turn on ExpThrower module
        if not expThrower:isActive() then
            expThrower:toggle()
        end
        repairing = true -- Set the repairing flag to true
    elseif not needsRepair and repairing then
        -- Turn off ExpThrower module
        if expThrower:isActive() then
            expThrower:toggle()
        end
        repairing = false -- Set the repairing flag to false
    end

    -- Check if the item is fully repaired or if we run out of experience bottles
    local fullyRepaired = true
    for _, itemStack in ipairs(itemsToCheck) do
        if not (itemStack == nil) and not (itemStack:isEmpty()) and itemStack:isDamageable() then
            if itemStack:getDamage() > 0 then
                fullyRepaired = false -- Set fullyRepaired to false if any item still needs repair
                break
            end
        end
    end
end

-- These functions are automatically called in java when we load or unload the script.

-- Function to register the autoRepair function to the PlayerTick event
function onRun()
    eventManager:register("PlayerTick", autoRepair)
end

-- Function to unregister the autoRepair function from the PlayerTick event
function onStop()
    eventManager:unregister("PlayerTick", autoRepair)
end
